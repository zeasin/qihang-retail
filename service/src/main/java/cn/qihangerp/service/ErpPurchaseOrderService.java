package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpPurchaseOrder;
import cn.qihangerp.model.request.PurchaseOrderAddRequest;
import cn.qihangerp.model.request.PurchaseOrderOptionRequest;
import cn.qihangerp.model.request.SearchRequest;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qilip
* @description 针对表【scm_purchase_order(采购订单)】的数据库操作Service
* @createDate 2024-10-20 15:36:33
*/
public interface ErpPurchaseOrderService extends IService<ErpPurchaseOrder> {
    PageResult<ErpPurchaseOrder> queryPageList(SearchRequest bo, PageQuery pageQuery);
    ErpPurchaseOrder getDetailById(Long id);
    ResultVo<Long> createPurchaseOrder(PurchaseOrderAddRequest addBo);
    int updateScmPurchaseOrder(PurchaseOrderOptionRequest request);
}
