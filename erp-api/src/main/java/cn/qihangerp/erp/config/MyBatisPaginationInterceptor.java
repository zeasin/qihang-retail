package cn.qihangerp.erp.config;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ParameterUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * MyBatis-Plus 3.5.16 分页拦截器
 * 替代已移除的 PaginationInnerInterceptor
 */
public class MyBatisPaginationInterceptor implements InnerInterceptor {

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        IPage<?> page = ParameterUtils.findPage(parameter).orElse(null);
        if (page == null || ms.getSqlCommandType() != SqlCommandType.SELECT) {
            return;
        }
        long size = page.getSize();
        if (size <= 0) return;

        String originalSql = boundSql.getSql();

        // 1. COUNT 查询（使用原始SQL替换SELECT和去除ORDER BY/LIMIT）
        try {
            String countSql = buildCountSql(originalSql);
            Connection connection = executor.getTransaction().getConnection();
            try (PreparedStatement ps = connection.prepareStatement(countSql)) {
                // 复用原始SQL的参数设置方式
                org.apache.ibatis.executor.parameter.ParameterHandler ph = ms.getConfiguration()
                        .newParameterHandler(ms, parameter, boundSql);
                ph.setParameters(ps);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        page.setTotal(rs.getLong(1));
                    }
                }
            }
        } catch (Exception e) {
            // COUNT失败时不影响主查询
            System.err.println("COUNT查询失败: " + e.getMessage());
        }

        // 2. LIMIT
        long offset = (page.getCurrent() > 0) ? (page.getCurrent() - 1) * size : 0;
        try {
            java.lang.reflect.Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, originalSql + " LIMIT " + offset + ", " + size);
        } catch (Exception e) {
            throw new RuntimeException("SQL分页改写失败", e);
        }
    }

    private String buildCountSql(String sql) {
        // 去除末尾的 LIMIT 子句
        sql = sql.replaceAll("(?i)\\s+LIMIT\\s+\\d+(?:\\s*,\\s*\\d+)?\\s*$", "");
        // 去除 ORDER BY 子句（在子查询中 ORDER BY 无意义）
        sql = sql.replaceAll("(?i)\\s+ORDER\\s+BY\\s+[^)]+?(?=\\s*(?:LIMIT|$))", "");
        // 替换 SELECT ... FROM 为 SELECT COUNT(*) FROM
        // 处理 SELECT DISTINCT / SELECT 等
        int fromIdx = sql.toUpperCase().indexOf(" FROM ");
        if (fromIdx > 0) {
            return "SELECT COUNT(*) " + sql.substring(fromIdx);
        }
        return "SELECT COUNT(*) FROM (" + sql + ") tmp_count";
    }

    @Override public void beforePrepare(StatementHandler sh, Connection connection, Integer transactionTimeout) {}
    @Override public void setProperties(Properties properties) {}
}
