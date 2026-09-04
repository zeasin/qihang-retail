package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.request.ShipDeliveryRequest;
import cn.qihangerp.model.request.ShipStockingSearchRequest;

import java.util.Map;

public interface ShipStockingService {

    PageResult<OOrder> queryPendingList(ShipStockingSearchRequest query, PageQuery pageQuery);

    OOrder getOrderDetail(String id);

    ResultVo<String> executeDelivery(ShipDeliveryRequest request, String username);

    ResultVo<String> executePickup(String orderId, String username);

    Map<String, Object> getStats();
}
