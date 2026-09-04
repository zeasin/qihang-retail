package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.request.ManualShipSearchRequest;
import cn.qihangerp.model.vo.ManualShipStatsVo;

import java.util.List;

public interface ManualShipService {

    PageResult<OOrder> queryPickupOrders(ManualShipSearchRequest query, PageQuery pageQuery);

    OOrder getOrderDetail(String id);

    ManualShipStatsVo getStats();

    ResultVo<String> confirmPickup(String orderId, String username);

    ResultVo<String> batchConfirmPickup(List<String> orderIds, String username);
}
