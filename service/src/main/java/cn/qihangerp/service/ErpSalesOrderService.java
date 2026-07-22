package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpSalesOrder;
import cn.qihangerp.model.entity.ErpSalesOrderItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 销售单服务接口
 */
public interface ErpSalesOrderService extends IService<ErpSalesOrder> {

    /**
     * 分页查询销售单列表
     */
    PageResult<ErpSalesOrder> queryPageList(ErpSalesOrder query, PageQuery pageQuery);

    /**
     * 查询销售单明细
     */
    List<ErpSalesOrderItem> selectItemsByOrderId(Long orderId);
}
