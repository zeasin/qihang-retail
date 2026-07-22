package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.OOrderStocking;
import cn.qihangerp.model.entity.OOrderStockingItem;
import cn.qihangerp.model.bo.StockingOrderBo;
import cn.qihangerp.request.ShipRecordQueryRequest;
import cn.qihangerp.request.SupplierShipOrderSearchRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface OOrderStockingService extends IService<OOrderStocking> {
    ResultVo<String> pushOrderToCloudWarehouse(List<Long> orderIds);
    ResultVo<String> pushOrderItemToCloudWarehouseByIds(List<Long> itemIds);
    PageResult<OOrderStocking> queryPageList(SupplierShipOrderSearchRequest bo, PageQuery pageQuery);
    PageResult<OOrderStocking> querySupplierShipPageList(SupplierShipOrderSearchRequest bo, PageQuery pageQuery);
    PageResult<OOrderStocking> queryStockUpPageList(StockingOrderBo request, PageQuery pageQuery);
    OOrderStocking queryDetailById(Long id);
    List<OOrderStockingItem> getItemsByOrderId(Long shipOrderId);
    List<OOrderStockingItem> getItemsByOrderNum(String orderNum);
    List<OOrderStocking> getByOrderNum(String orderNum);
    PageResult<OOrderStocking> queryShipRecordPageList(ShipRecordQueryRequest request, PageQuery pageQuery);
}
