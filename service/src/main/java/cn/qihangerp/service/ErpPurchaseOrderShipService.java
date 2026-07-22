package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpPurchaseOrderShip;
import cn.qihangerp.model.request.PurchaseOrderStockInBo;
import cn.qihangerp.model.request.SearchRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qilip
* @description 针对表【scm_purchase_order_ship(采购订单物流表)】的数据库操作Service
* @createDate 2024-10-20 17:18:53
*/
public interface ErpPurchaseOrderShipService extends IService<ErpPurchaseOrderShip> {
    PageResult<ErpPurchaseOrderShip> queryPageList(SearchRequest bo, PageQuery pageQuery);

    int updateScmPurchaseOrderShip(ErpPurchaseOrderShip erpPurchaseOrderShip);

    ResultVo<Long> createStockInEntry(PurchaseOrderStockInBo bo,Long userId,String userName);
}
