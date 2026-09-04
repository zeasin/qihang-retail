package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.enums.EnumDeliveryMethod;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.entity.OOrderItem;
import cn.qihangerp.model.request.ManualShipSearchRequest;
import cn.qihangerp.model.vo.ManualShipStatsVo;
import cn.qihangerp.service.ManualShipService;
import cn.qihangerp.service.OOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ManualShipServiceImpl implements ManualShipService {

    private final OOrderService orderService;

    @Override
    public PageResult<OOrder> queryPickupOrders(ManualShipSearchRequest query, PageQuery pageQuery) {
        Page<OOrder> page = pageQuery.build();
        LambdaQueryWrapper<OOrder> wrapper = buildPickupWrapper(query);
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
    public ManualShipStatsVo getStats() {
        ManualShipStatsVo stats = new ManualShipStatsVo();

        long pendingCount = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.PICKUP.getCode())
                .eq(OOrder::getShipStatus, 0)
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.setPendingCount(pendingCount);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        long todayConfirmed = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.PICKUP.getCode())
                .eq(OOrder::getShipStatus, 2)
                .ge(OOrder::getUpdateTime, todayStart)
                .le(OOrder::getUpdateTime, todayEnd));
        stats.setTodayConfirmed(todayConfirmed);

        long todayTotal = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.PICKUP.getCode())
                .ge(OOrder::getCreateTime, todayStart)
                .le(OOrder::getCreateTime, todayEnd));
        stats.setTodayTotal(todayTotal);

        long totalPending = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.PICKUP.getCode())
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.setTotalPending(totalPending);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<String> confirmPickup(String orderId, String username) {
        if (orderId == null) return ResultVo.error("订单ID不能为空");

        OOrder order = orderService.getById(orderId);
        if (order == null) return ResultVo.error("订单不存在");
        if (order.getShipStatus() == 2) return ResultVo.error("订单已提货");

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

        log.info("订单{}已确认提货，操作人={}", order.getOrderNum(), username);
        return ResultVo.success(order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<String> batchConfirmPickup(List<String> orderIds, String username) {
        if (orderIds == null || orderIds.isEmpty()) {
            return ResultVo.error("请选择订单");
        }
        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;
        for (String orderId : orderIds) {
            OOrder order = orderService.getById(orderId);
            if (order == null) continue;
            if (order.getShipStatus() == 2) continue;

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
            successCount++;
        }
        log.info("批量确认提货，成功={}，操作人={}", successCount, username);
        return ResultVo.success("成功确认 " + successCount + " 个订单");
    }

    private LambdaQueryWrapper<OOrder> buildPickupWrapper(ManualShipSearchRequest query) {
        LambdaQueryWrapper<OOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.PICKUP.getCode());
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
        if (query.getShipStatus() != null) {
            wrapper.eq(OOrder::getShipStatus, query.getShipStatus());
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
