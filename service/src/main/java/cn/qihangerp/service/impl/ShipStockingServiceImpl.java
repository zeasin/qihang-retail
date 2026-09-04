package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.enums.EnumDeliveryMethod;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.entity.OOrderItem;
import cn.qihangerp.model.request.ShipDeliveryRequest;
import cn.qihangerp.model.request.ShipStockingSearchRequest;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.service.ShipStockingService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class ShipStockingServiceImpl implements ShipStockingService {

    private final OOrderService orderService;

    @Override
    public PageResult<OOrder> queryPendingList(ShipStockingSearchRequest query, PageQuery pageQuery) {
        Page<OOrder> page = pageQuery.build();
        LambdaQueryWrapper<OOrder> wrapper = buildPendingWrapper(query);
        Page<OOrder> result = orderService.page(page, wrapper);
        List<OOrder> records = result.getRecords();
        for (OOrder order : records) {
            List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
            order.setItemList(items);
        }
        return PageResult.build(result);
    }

    @Override
    public OOrder getOrderDetail(String id) {
        OOrder order = orderService.getById(id);
        if (order != null) {
            List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
            order.setItemList(items);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<String> executeDelivery(ShipDeliveryRequest request, String username) {
        if (request.getOrderId() == null) {
            return ResultVo.error("订单ID不能为空");
        }
        if (request.getDeliveryMethod() == null) {
            return ResultVo.error("配送方式不能为空");
        }

        OOrder order = orderService.getById(request.getOrderId());
        if (order == null) return ResultVo.error("订单不存在");
        if (order.getShipStatus() == 2) return ResultVo.error("订单已发货");

        LocalDateTime now = LocalDateTime.now();

        OOrder updateOrder = new OOrder();
        updateOrder.setId(order.getId());
        updateOrder.setDeliveryMethod(request.getDeliveryMethod());
        updateOrder.setShipStatus(2);
        updateOrder.setWaybillCode(request.getTrackingNumber());
        updateOrder.setOrderModifiedTime(String.valueOf(now));
        updateOrder.setUpdateBy(username);
        updateOrder.setUpdateTime(now);
        orderService.updateById(updateOrder);

        List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
        for (OOrderItem item : items) {
            OOrderItem updateItem = new OOrderItem();
            updateItem.setId(item.getId());
            updateItem.setShipStatus(2);
            updateItem.setUpdateBy(username);
            updateItem.setUpdateTime(now);
            orderService.updateOrderItem(updateItem);
        }

        log.info("订单{}已执行配送，配送方式={}，承运人={}，运单号={}", order.getOrderNum(),
                request.getDeliveryMethod(), request.getCarrierName(), request.getTrackingNumber());
        return ResultVo.success(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<String> executePickup(String orderId, String username) {
        if (orderId == null) return ResultVo.error("订单ID不能为空");

        OOrder order = orderService.getById(orderId);
        if (order == null) return ResultVo.error("订单不存在");
        if (order.getShipStatus() == 2) return ResultVo.error("订单已发货");

        LocalDateTime now = LocalDateTime.now();

        OOrder updateOrder = new OOrder();
        updateOrder.setId(order.getId());
        updateOrder.setDeliveryMethod(EnumDeliveryMethod.PICKUP.getCode());
        updateOrder.setShipStatus(2);
        updateOrder.setOrderModifiedTime(String.valueOf(now));
        updateOrder.setUpdateBy(username);
        updateOrder.setUpdateTime(now);
        orderService.updateById(updateOrder);

        List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
        for (OOrderItem item : items) {
            OOrderItem updateItem = new OOrderItem();
            updateItem.setId(item.getId());
            updateItem.setShipStatus(2);
            updateItem.setUpdateBy(username);
            updateItem.setUpdateTime(now);
            orderService.updateOrderItem(updateItem);
        }

        log.info("订单{}已到店自提", order.getOrderNum());
        return ResultVo.success(order.getId());
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        long pendingCount = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getShipStatus, 0)
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.put("pendingCount", pendingCount);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        long todayShipped = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getShipStatus, 2)
                .ge(OOrder::getUpdateTime, todayStart)
                .le(OOrder::getUpdateTime, todayEnd));
        stats.put("todayShipped", todayShipped);

        long riderCount = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getShipStatus, 0)
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.RIDER.getCode())
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.put("riderCount", riderCount);

        long merchantCount = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getShipStatus, 0)
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.MERCHANT.getCode())
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.put("merchantCount", merchantCount);

        long pickupCount = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getShipStatus, 0)
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.PICKUP.getCode())
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.put("pickupCount", pickupCount);

        return stats;
    }

    private LambdaQueryWrapper<OOrder> buildPendingWrapper(ShipStockingSearchRequest query) {
        LambdaQueryWrapper<OOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OOrder::getShipStatus, 0);
        wrapper.in(OOrder::getOrderStatus, 0, 1, 2, 3);

        if (StringUtils.hasText(query.getOrderNum())) {
            wrapper.like(OOrder::getOrderNum, query.getOrderNum());
        }
        if (StringUtils.hasText(query.getReceiverName())) {
            wrapper.like(OOrder::getReceiverName, query.getReceiverName());
        }
        if (StringUtils.hasText(query.getReceiverMobile())) {
            wrapper.like(OOrder::getReceiverMobile, query.getReceiverMobile());
        }
        if (query.getDeliveryMethod() != null) {
            wrapper.eq(OOrder::getDeliveryMethod, query.getDeliveryMethod());
        }
        if (query.getOrderStatus() != null) {
            wrapper.eq(OOrder::getOrderStatus, query.getOrderStatus());
        }
        if (StringUtils.hasText(query.getStartTime())) {
            wrapper.ge(OOrder::getCreateTime, LocalDate.parse(query.getStartTime()).atStartOfDay());
        }
        if (StringUtils.hasText(query.getEndTime())) {
            wrapper.le(OOrder::getCreateTime, LocalDate.parse(query.getEndTime()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(OOrder::getCreateTime);
        return wrapper;
    }
}
