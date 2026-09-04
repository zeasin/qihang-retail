package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.enums.EnumDeliveryMethod;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.entity.OOrderItem;
import cn.qihangerp.model.request.RiderDeliverySearchRequest;
import cn.qihangerp.model.vo.RiderDeliveryStatsVo;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.service.RiderDeliveryService;
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
public class RiderDeliveryServiceImpl implements RiderDeliveryService {

    private final OOrderService orderService;

    @Override
    public PageResult<OOrder> queryRiderOrders(RiderDeliverySearchRequest query, PageQuery pageQuery) {
        Page<OOrder> page = pageQuery.build();
        LambdaQueryWrapper<OOrder> wrapper = buildRiderWrapper(query);
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
    public RiderDeliveryStatsVo getStats() {
        RiderDeliveryStatsVo stats = new RiderDeliveryStatsVo();

        long pendingCount = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.RIDER.getCode())
                .eq(OOrder::getShipStatus, 0)
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.setPendingCount(pendingCount);

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        long todayPrinted = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.RIDER.getCode())
                .eq(OOrder::getWaybillStatus, 2)
                .ge(OOrder::getUpdateTime, todayStart)
                .le(OOrder::getUpdateTime, todayEnd));
        stats.setTodayPrinted(todayPrinted);

        long todayShipped = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.RIDER.getCode())
                .eq(OOrder::getShipStatus, 2)
                .ge(OOrder::getUpdateTime, todayStart)
                .le(OOrder::getUpdateTime, todayEnd));
        stats.setTodayShipped(todayShipped);

        long totalPending = orderService.count(new LambdaQueryWrapper<OOrder>()
                .eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.RIDER.getCode())
                .in(OOrder::getWaybillStatus, 0, 1)
                .in(OOrder::getOrderStatus, 0, 1, 2, 3));
        stats.setTotalPending(totalPending);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<String> batchPrinted(List<String> orderIds, String username) {
        if (orderIds == null || orderIds.isEmpty()) {
            return ResultVo.error("请选择订单");
        }
        LocalDateTime now = LocalDateTime.now();
        for (String orderId : orderIds) {
            OOrder order = orderService.getById(orderId);
            if (order == null) continue;
            if (order.getWaybillStatus() != null && order.getWaybillStatus() >= 2) continue;

            OOrder update = new OOrder();
            update.setId(order.getId());
            update.setWaybillStatus(2);
            update.setUpdateBy(username);
            update.setUpdateTime(now);
            orderService.updateById(update);
        }
        log.info("骑手配送批量打印，订单数={}，操作人={}", orderIds.size(), username);
        return ResultVo.success("操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<String> batchShip(List<String> orderIds, String username) {
        if (orderIds == null || orderIds.isEmpty()) {
            return ResultVo.error("请选择订单");
        }
        LocalDateTime now = LocalDateTime.now();
        for (String orderId : orderIds) {
            OOrder order = orderService.getById(orderId);
            if (order == null) continue;
            if (order.getShipStatus() == 2) continue;

            OOrder update = new OOrder();
            update.setId(order.getId());
            update.setShipStatus(2);
            update.setWaybillStatus(3);
            update.setUpdateBy(username);
            update.setUpdateTime(now);
            orderService.updateById(update);

            List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
            for (OOrderItem item : items) {
                OOrderItem updateItem = new OOrderItem();
                updateItem.setId(item.getId());
                updateItem.setShipStatus(2);
                updateItem.setUpdateBy(username);
                updateItem.setUpdateTime(now);
                orderService.updateOrderItem(updateItem);
            }
        }
        log.info("骑手配送批量发货，订单数={}，操作人={}", orderIds.size(), username);
        return ResultVo.success("操作成功");
    }

    private LambdaQueryWrapper<OOrder> buildRiderWrapper(RiderDeliverySearchRequest query) {
        LambdaQueryWrapper<OOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OOrder::getDeliveryMethod, EnumDeliveryMethod.RIDER.getCode());
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
        if (query.getWaybillStatus() != null) {
            wrapper.eq(OOrder::getWaybillStatus, query.getWaybillStatus());
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
