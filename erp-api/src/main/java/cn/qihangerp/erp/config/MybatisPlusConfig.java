package cn.qihangerp.erp.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@MapperScan({"cn.qihangerp.mapper","cn.qihangerp.module.mapper","cn.qihangerp.mapper"})
public class MybatisPlusConfig {
    @Primary
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // MyBatis-Plus 3.5.16 移除了 PaginationInnerInterceptor，使用自定义拦截器
        interceptor.addInnerInterceptor(new MyBatisPaginationInterceptor());
        return interceptor;
    }
}
