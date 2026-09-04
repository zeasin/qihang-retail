package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.enums.EnumAfterSaleStatus;
import cn.qihangerp.enums.EnumAfterSaleType;
import cn.qihangerp.mapper.ORefundMapper;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.request.AfterSaleApplyRequest;
import cn.qihangerp.model.request.AfterSaleAuditRequest;
import cn.qihangerp.model.request.AfterSaleProcessRequest;
import cn.qihangerp.model.request.AfterSaleSearchRequest;
import cn.qihangerp.model.vo.AfterSaleConfigVo;
import cn.qihangerp.model.vo.AfterSaleStatsVo;
import cn.qihangerp.mapper.OOrderItemMapper;
import cn.qihangerp.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 售后退款服务实现
 */
@Slf4j
@AllArgsConstructor
@Service
public class ORefundServiceImpl extends ServiceImpl<ORefundMapper, ORefund> implements ORefundService {

    private final OOrderService orderService;
    private final OGoodsInventoryService goodsInventoryService;
    private final SysConfigService configService;
    private final OOrderItemMapper orderItemMapper;

    @Override
    public PageResult<ORefund> queryPageList(ORefund query, PageQuery pageQuery) {
        Page<ORefund> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<ORefund> wrapper = buildQueryWrapper(query);
        Page<ORefund> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    @Override
    public PageResult<ORefund> queryAfterSalePageList(AfterSaleSearchRequest query, PageQuery pageQuery) {
        Page<ORefund> page = pageQuery.build();
        LambdaQueryWrapper<ORefund> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getRefundNum())) {
            wrapper.like(ORefund::getRefundNum, query.getRefundNum());
        }
        if (StringUtils.hasText(query.getOrderNum())) {
            wrapper.like(ORefund::getOrderNum, query.getOrderNum());
        }
        if (query.getRefundType() != null) {
            wrapper.eq(ORefund::getRefundType, query.getRefundType());
        }
        if (query.getErpStatus() != null) {
            wrapper.eq(ORefund::getErpStatus, query.getErpStatus());
        }
        if (query.getShopId() != null) {
            wrapper.eq(ORefund::getShopId, query.getShopId());
        }
        if (StringUtils.hasText(query.getGoodsName())) {
            wrapper.like(ORefund::getGoodsName, query.getGoodsName());
        }
        if (StringUtils.hasText(query.getStartTime())) {
            wrapper.ge(ORefund::getCreateTime, LocalDate.parse(query.getStartTime()).atStartOfDay());
        }
        if (StringUtils.hasText(query.getEndTime())) {
            wrapper.le(ORefund::getCreateTime, LocalDate.parse(query.getEndTime()).atTime(23, 59, 59));
        }
        wrapper.orderByDesc(ORefund::getCreateTime);
        Page<ORefund> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    @Override
    public ORefund getAfterSaleDetail(Long id) {
        return this.getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> createAfterSale(AfterSaleApplyRequest request, String username) {
        if (request.getOrderId() == null || request.getOrderItemId() == null) {
            return ResultVo.error("订单和订单项不能为空");
        }
        if (request.getRefundType() == null) {
            return ResultVo.error("售后类型不能为空");
        }
        EnumAfterSaleType saleType = EnumAfterSaleType.fromCode(request.getRefundType());
        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            return ResultVo.error("退货/退款数量必须大于0");
        }

        OOrder order = orderService.getById(request.getOrderId());
        if (order == null) return ResultVo.error("订单不存在");
        if (order.getOrderStatus() != 3 && order.getOrderStatus() != 0) {
            return ResultVo.error("订单状态不允许售后，仅已完成或新订单可发起售后");
        }

        List<OOrderItem> items = orderService.selectItemsByOrderId(String.valueOf(order.getId()));
        OOrderItem targetItem = null;
        for (OOrderItem item : items) {
            if (item.getId().equals(String.valueOf(request.getOrderItemId()))) {
                targetItem = item;
                break;
            }
        }
        if (targetItem == null) return ResultVo.error("订单项不存在");

        int refundableQty = targetItem.getQuantity() - (targetItem.getRefundCount() != null ? targetItem.getRefundCount() : 0);
        if (request.getQuantity() > refundableQty) {
            return ResultVo.error("可退数量不足，最多可退" + refundableQty + "件");
        }

        String authCheck = checkAuthorizationPeriod(order, saleType);
        if (authCheck != null) return ResultVo.error(authCheck);

        if (saleType == EnumAfterSaleType.EXCHANGE) {
            if (request.getExchangeGoodsSkuId() == null) {
                return ResultVo.error("换货必须选择换货商品规格");
            }
        }

        double auditThreshold = getConfigDouble("retail.refund.audit_threshold", 200.0);
        boolean needAudit = request.getRefundFee() != null && request.getRefundFee() > auditThreshold;

        LocalDateTime now = LocalDateTime.now();
        String refundNum = "AS" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", (int) (Math.random() * 10000));

        ORefund refund = new ORefund();
        refund.setRefundNum(refundNum);
        refund.setRefundType(request.getRefundType());
        refund.setShopId(order.getShopId());
        refund.setShopType(order.getShopType());
        refund.setOrderAmount(order.getAmount());
        refund.setRefundFee(request.getRefundFee() != null ? request.getRefundFee().floatValue() : 0f);
        refund.setRefundReason(request.getRefundReason());
        refund.setOrderNum(order.getOrderNum());
        refund.setOrderItemNum(targetItem.getId());
        refund.setGoodsId(targetItem.getGoodsId());
        refund.setGoodsSkuId(targetItem.getGoodsSkuId());
        refund.setGoodsName(targetItem.getGoodsTitle());
        refund.setGoodsSku(targetItem.getGoodsSpec());
        refund.setGoodsImage(targetItem.getGoodsImg());
        refund.setHasGoodReturn(saleType == EnumAfterSaleType.REFUND_ONLY ? 0 : 1);
        refund.setQuantity((long) request.getQuantity());
        refund.setRemark(request.getRemark());
        refund.setStatus(10090);
        refund.setHasProcessing(0);
        refund.setMerchantId(order.getMerchantId());
        refund.setCreateTime(now);
        refund.setCreateBy(username);

        if (needAudit) {
            refund.setErpStatus(EnumAfterSaleStatus.PENDING_AUDIT.getCode());
        } else {
            if (saleType == EnumAfterSaleType.REFUND_ONLY) {
                refund.setErpStatus(EnumAfterSaleStatus.PENDING_REFUND.getCode());
            } else {
                refund.setErpStatus(EnumAfterSaleStatus.PENDING_RETURN.getCode());
            }
        }

        if (saleType == EnumAfterSaleType.EXCHANGE) {
            refund.setExchangeErpGoodsSkuId(request.getExchangeGoodsSkuId());
            refund.setExchangeGoodsNum(request.getExchangeQuantity());
        }

        this.save(refund);

        OOrderItem updateItem = new OOrderItem();
        updateItem.setId(targetItem.getId());
        updateItem.setRefundStatus(2);
        orderService.updateOrderItem(updateItem);

        return ResultVo.success(refund.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> auditAfterSale(Long id, AfterSaleAuditRequest request, String username) {
        ORefund refund = this.getById(id);
        if (refund == null) return ResultVo.error("售后单不存在");
        if (refund.getErpStatus() != EnumAfterSaleStatus.PENDING_AUDIT.getCode()) {
            return ResultVo.error("当前状态不允许审核");
        }

        LocalDateTime now = LocalDateTime.now();
        EnumAfterSaleType saleType = EnumAfterSaleType.fromCode(refund.getRefundType());

        if (request.getApproved() != null && request.getApproved()) {
            if (saleType == EnumAfterSaleType.REFUND_ONLY) {
                refund.setErpStatus(EnumAfterSaleStatus.PENDING_REFUND.getCode());
            } else {
                refund.setErpStatus(EnumAfterSaleStatus.PENDING_RETURN.getCode());
            }
        } else {
            refund.setErpStatus(EnumAfterSaleStatus.REJECTED.getCode());
            OOrderItem updateItem = new OOrderItem();
            updateItem.setId(refund.getOrderItemNum());
            updateItem.setRefundStatus(1);
            orderService.updateOrderItem(updateItem);
        }

        refund.setAuditBy(username);
        refund.setAuditTime(now);
        refund.setAuditRemark(request.getRemark());
        refund.setUpdateBy(username);
        refund.setUpdateTime(now);
        this.updateById(refund);

        return ResultVo.success(refund.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> receiveReturnGoods(Long id, String username) {
        ORefund refund = this.getById(id);
        if (refund == null) return ResultVo.error("售后单不存在");
        if (refund.getErpStatus() != EnumAfterSaleStatus.PENDING_RETURN.getCode()) {
            return ResultVo.error("当前状态不允许收货，需先审核通过");
        }

        LocalDateTime now = LocalDateTime.now();
        EnumAfterSaleType saleType = EnumAfterSaleType.fromCode(refund.getRefundType());

        addStockBackToInventory(refund, username, now);

        if (saleType == EnumAfterSaleType.EXCHANGE) {
            refund.setErpStatus(EnumAfterSaleStatus.PENDING_EXCHANGE.getCode());
        } else {
            refund.setErpStatus(EnumAfterSaleStatus.PENDING_REFUND.getCode());
        }
        refund.setReceiveTime(now);
        refund.setReceiveBy(username);
        refund.setUpdateBy(username);
        refund.setUpdateTime(now);
        this.updateById(refund);

        return ResultVo.success(refund.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> processAfterSale(Long id, AfterSaleProcessRequest request, String username) {
        ORefund refund = this.getById(id);
        if (refund == null) return ResultVo.error("售后单不存在");
        if (refund.getErpStatus() != EnumAfterSaleStatus.PENDING_REFUND.getCode()) {
            return ResultVo.error("当前状态不允许执行退款");
        }

        EnumAfterSaleType saleType = EnumAfterSaleType.fromCode(refund.getRefundType());
        if (saleType == EnumAfterSaleType.RETURN_REFUND && refund.getReceiveTime() == null) {
            return ResultVo.error("退货退款需先收货再退款");
        }

        LocalDateTime now = LocalDateTime.now();

        OOrderItem item = findOrderItem(refund.getOrderItemNum());
        if (item != null) {
            int newRefundCount = (item.getRefundCount() != null ? item.getRefundCount() : 0) + refund.getQuantity().intValue();
            OOrderItem updateItem = new OOrderItem();
            updateItem.setId(item.getId());
            updateItem.setRefundCount(newRefundCount);
            updateItem.setRefundStatus(newRefundCount >= item.getQuantity() ? 4 : 3);
            updateItem.setUpdateBy(username);
            updateItem.setUpdateTime(now);
            orderService.updateOrderItem(updateItem);
        }

        if (saleType == EnumAfterSaleType.REFUND_ONLY) {
            unlockInventory(refund);
        }

        refund.setErpStatus(EnumAfterSaleStatus.COMPLETED.getCode());
        refund.setStatus(10010);
        refund.setRefundMethod(request != null ? request.getRefundMethod() : "cash");
        refund.setRefundTime(now);
        refund.setRefundBy(username);
        refund.setHasProcessing(1);
        refund.setUpdateBy(username);
        refund.setUpdateTime(now);
        if (request != null && StringUtils.hasText(request.getRemark())) {
            refund.setRemark(request.getRemark());
        }
        this.updateById(refund);

        return ResultVo.success(refund.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> shipExchange(Long id, String username) {
        ORefund refund = this.getById(id);
        if (refund == null) return ResultVo.error("售后单不存在");
        if (refund.getErpStatus() != EnumAfterSaleStatus.PENDING_EXCHANGE.getCode()) {
            return ResultVo.error("当前状态不允许换货发货");
        }

        if (refund.getExchangeErpGoodsSkuId() == null) {
            return ResultVo.error("换货商品规格未指定");
        }

        OGoodsInventory exchangeInv = goodsInventoryService.getOne(
                new LambdaQueryWrapper<OGoodsInventory>()
                        .eq(OGoodsInventory::getSkuId, refund.getExchangeErpGoodsSkuId()));
        if (exchangeInv == null) {
            return ResultVo.error("换货商品库存记录不存在");
        }
        int exchangeQty = refund.getExchangeGoodsNum() != null ? refund.getExchangeGoodsNum() : refund.getQuantity().intValue();
        if (exchangeInv.getAvailableQuantity() < exchangeQty) {
            return ResultVo.error("换货商品可用库存不足，当前可用：" + exchangeInv.getAvailableQuantity());
        }
        goodsInventoryService.deductStock(exchangeInv.getId(), exchangeQty);

        LocalDateTime now = LocalDateTime.now();
        refund.setErpStatus(EnumAfterSaleStatus.COMPLETED.getCode());
        refund.setStatus(10010);
        refund.setShippingStatus(1);
        refund.setHasProcessing(1);
        refund.setUpdateBy(username);
        refund.setUpdateTime(now);
        this.updateById(refund);

        return ResultVo.success(refund.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> cancelAfterSale(Long id, String username) {
        ORefund refund = this.getById(id);
        if (refund == null) return ResultVo.error("售后单不存在");
        if (refund.getErpStatus() == EnumAfterSaleStatus.COMPLETED.getCode()) {
            return ResultVo.error("已完成的售后不可取消");
        }
        if (refund.getErpStatus() == EnumAfterSaleStatus.CANCELLED.getCode()) {
            return ResultVo.error("售后单已取消");
        }

        LocalDateTime now = LocalDateTime.now();
        refund.setErpStatus(EnumAfterSaleStatus.CANCELLED.getCode());
        refund.setStatus(10011);
        refund.setUpdateBy(username);
        refund.setUpdateTime(now);
        this.updateById(refund);

        OOrderItem item = findOrderItem(refund.getOrderItemNum());
        if (item != null && item.getRefundStatus() != null && item.getRefundStatus() == 2) {
            OOrderItem updateItem = new OOrderItem();
            updateItem.setId(item.getId());
            int currentRefundCount = item.getRefundCount() != null ? item.getRefundCount() : 0;
            updateItem.setRefundStatus(currentRefundCount > 0 ? 3 : 1);
            updateItem.setUpdateBy(username);
            updateItem.setUpdateTime(now);
            orderService.updateOrderItem(updateItem);
        }

        return ResultVo.success(refund.getId());
    }

    @Override
    public AfterSaleStatsVo getAfterSaleStats() {
        AfterSaleStatsVo stats = new AfterSaleStatsVo();
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        long todayCount = this.count(new LambdaQueryWrapper<ORefund>()
                .ge(ORefund::getCreateTime, todayStart)
                .le(ORefund::getCreateTime, todayEnd));
        stats.setTodayCount(todayCount);

        List<ORefund> todayRefunds = this.list(new LambdaQueryWrapper<ORefund>()
                .eq(ORefund::getErpStatus, EnumAfterSaleStatus.COMPLETED.getCode())
                .ge(ORefund::getCreateTime, todayStart)
                .le(ORefund::getCreateTime, todayEnd));
        double todayRefundAmount = todayRefunds.stream()
                .mapToDouble(r -> r.getRefundFee() != null ? r.getRefundFee() : 0f)
                .sum();
        stats.setTodayRefundAmount(Math.round(todayRefundAmount * 100) / 100.0);

        long pendingAudit = this.count(new LambdaQueryWrapper<ORefund>()
                .eq(ORefund::getErpStatus, EnumAfterSaleStatus.PENDING_AUDIT.getCode()));
        stats.setPendingAudit(pendingAudit);

        long pendingProcess = this.count(new LambdaQueryWrapper<ORefund>()
                .in(ORefund::getErpStatus,
                        EnumAfterSaleStatus.PENDING_RETURN.getCode(),
                        EnumAfterSaleStatus.PENDING_REFUND.getCode(),
                        EnumAfterSaleStatus.PENDING_EXCHANGE.getCode()));
        stats.setPendingProcess(pendingProcess);

        long totalCompleted = this.count(new LambdaQueryWrapper<ORefund>()
                .eq(ORefund::getErpStatus, EnumAfterSaleStatus.COMPLETED.getCode()));
        stats.setTotalCompleted(totalCompleted);

        return stats;
    }

    @Override
    public AfterSaleConfigVo getAfterSaleConfig() {
        AfterSaleConfigVo config = new AfterSaleConfigVo();
        config.setReturnPeriodDays(getConfigInt("retail.return.period_days", 7));
        config.setExchangePeriodDays(getConfigInt("retail.exchange.period_days", 15));
        config.setRefundAuditThreshold(getConfigDouble("retail.refund.audit_threshold", 200.0));
        String reasons = getConfigString("retail.return.reasons",
                "商品质量问题,不想要了,商品描述不符,发错货,少件漏发,其他原因");
        config.setReturnReasons(reasons.split(","));
        return config;
    }

    private String checkAuthorizationPeriod(OOrder order, EnumAfterSaleType saleType) {
        int periodDays;
        if (saleType == EnumAfterSaleType.EXCHANGE) {
            periodDays = getConfigInt("retail.exchange.period_days", 15);
        } else {
            periodDays = getConfigInt("retail.return.period_days", 7);
        }

        LocalDateTime referenceTime = order.getCreateTime();
        if (order.getOrderTime() != null) {
            referenceTime = order.getOrderTime();
        }
        if (referenceTime == null) return null;

        long daysSinceOrder = java.time.Duration.between(referenceTime, LocalDateTime.now()).toDays();
        if (daysSinceOrder > periodDays) {
            return saleType == EnumAfterSaleType.EXCHANGE
                    ? "已超过换货期限（" + periodDays + "天），不可发起换货"
                    : "已超过退货退款期限（" + periodDays + "天），不可发起售后";
        }
        return null;
    }

    private void addStockBackToInventory(ORefund refund, String username, LocalDateTime now) {
        if (refund.getGoodsSkuId() == null) return;
        OGoodsInventory inv = goodsInventoryService.getOne(
                new LambdaQueryWrapper<OGoodsInventory>()
                        .eq(OGoodsInventory::getSkuId, refund.getGoodsSkuId()));
        if (inv != null) {
            goodsInventoryService.addStock(inv.getId(), refund.getQuantity().intValue());
        }
    }

    private void unlockInventory(ORefund refund) {
        if (refund.getGoodsSkuId() == null) return;
        OGoodsInventory inv = goodsInventoryService.getOne(
                new LambdaQueryWrapper<OGoodsInventory>()
                        .eq(OGoodsInventory::getSkuId, refund.getGoodsSkuId()));
        if (inv != null) {
            goodsInventoryService.lockStock(inv.getId(), -refund.getQuantity().intValue());
        }
    }

    private OOrderItem findOrderItem(String itemId) {
        if (!StringUtils.hasText(itemId)) return null;
        return orderItemMapper.selectById(itemId);
    }

    private int getConfigInt(String key, int defaultValue) {
        SysConfig config = configService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private double getConfigDouble(String key, double defaultValue) {
        SysConfig config = configService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            try {
                return Double.parseDouble(config.getConfigValue());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String getConfigString(String key, String defaultValue) {
        SysConfig config = configService.getOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, key));
        if (config != null && StringUtils.hasText(config.getConfigValue())) {
            return config.getConfigValue();
        }
        return defaultValue;
    }

    private LambdaQueryWrapper<ORefund> buildQueryWrapper(ORefund query) {
        LambdaQueryWrapper<ORefund> wrapper = new LambdaQueryWrapper<>();
        if (query.getShopId() != null) {
            wrapper.eq(ORefund::getShopId, query.getShopId());
        }
        if (query.getShopType() != null) {
            wrapper.eq(ORefund::getShopType, query.getShopType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ORefund::getStatus, query.getStatus());
        }
        if (query.getRefundType() != null) {
            wrapper.eq(ORefund::getRefundType, query.getRefundType());
        }
        if (StringUtils.hasText(query.getOrderNum())) {
            wrapper.like(ORefund::getOrderNum, query.getOrderNum());
        }
        if (StringUtils.hasText(query.getRefundNum())) {
            wrapper.like(ORefund::getRefundNum, query.getRefundNum());
        }
        if (StringUtils.hasText(query.getGoodsName())) {
            wrapper.like(ORefund::getGoodsName, query.getGoodsName());
        }
        wrapper.orderByDesc(ORefund::getCreateTime);
        return wrapper;
    }
}
