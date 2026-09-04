package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.request.RiderDeliverySearchRequest;
import cn.qihangerp.model.vo.RiderDeliveryStatsVo;

import java.util.List;

public interface RiderDeliveryService {

    PageResult<OOrder> queryRiderOrders(RiderDeliverySearchRequest query, PageQuery pageQuery);

    OOrder getOrderDetail(String id);

    RiderDeliveryStatsVo getStats();

    ResultVo<String> batchPrinted(List<String> orderIds, String username);

    ResultVo<String> batchShip(List<String> orderIds, String username);
}
