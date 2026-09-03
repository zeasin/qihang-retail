package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.entity.OOrderItem;
import cn.qihangerp.model.entity.ORefund;
import cn.qihangerp.model.entity.OGoodsInventory;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.service.ORefundService;
import cn.qihangerp.service.OGoodsInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * POS退款Controller
 */
@AllArgsConstructor
@RestController
@RequestMapping("/pos-api/refund")
public class PosRefundController extends BaseController {

    private final OOrderService orderService;
    private final ORefundService refundService;
    private final OGoodsInventoryService goodsInventoryService;

    /**
     * 查询可退款的POS订单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(OOrder query, PageQuery pageQuery) {
        query.setOrderSource("POS");
        LambdaQueryWrapper<OOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OOrder::getOrderSource, "POS");
        wrapper.in(OOrder::getOrderStatus, 0, 3);
        if (query.getShopId() != null) {
            wrapper.eq(OOrder::getShopId, query.getShopId());
        }
        if (query.getOrderNum() != null && !query.getOrderNum().isEmpty()) {
            wrapper.like(OOrder::getOrderNum, query.getOrderNum());
        }
        wrapper.orderByDesc(OOrder::getCreateTime);
        Page<OOrder> page = orderService.page(pageQuery.build(), wrapper);
        List<OOrder> records = page.getRecords();
        for (OOrder order : records) {
            List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
            order.setItemList(items);
        }
        return getDataTable(PageResult.build(page));
    }

    /**
     * 查询退款记录列表
     */
    @GetMapping("/records")
    public TableDataInfo records(ORefund query, PageQuery pageQuery) {
        PageResult<ORefund> pageList = refundService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 退款
     */
    @PostMapping
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult refund(@RequestBody RefundRequest request) {
        if (request.getOrderId() == null || request.getOrderItemId() == null) {
            return AjaxResult.error("订单和订单项不能为空");
        }

        OOrder order = orderService.getById(request.getOrderId());
        if (order == null) return AjaxResult.error("订单不存在");
        if (order.getOrderStatus() != 0 && order.getOrderStatus() != 3) {
            return AjaxResult.error("订单状态不可退款");
        }

        List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
        OOrderItem targetItem = null;
        for (OOrderItem item : items) {
            if (item.getId().equals(String.valueOf(request.getOrderItemId()))) {
                targetItem = item;
                break;
            }
        }
        if (targetItem == null) return AjaxResult.error("订单项不存在");

        int refundableQty = targetItem.getQuantity() - (targetItem.getRefundCount() != null ? targetItem.getRefundCount() : 0);
        if (request.getQuantity() > refundableQty) {
            return AjaxResult.error("可退数量不足，最多可退" + refundableQty);
        }

        String username = getUsername();
        LocalDateTime now = LocalDateTime.now();

        // 生成退款单号
        String refundNum = "RF" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + String.format("%04d", (int) (Math.random() * 10000));

        // 创建退款记录
        ORefund refund = new ORefund();
        refund.setRefundNum(refundNum);
        refund.setRefundType(request.getHasGoodReturn() == 1 ? 10 : 11);
        refund.setShopId(order.getShopId());
        refund.setShopType(order.getShopType());
        refund.setOrderAmount(order.getAmount());
        refund.setRefundFee(request.getRefundFee().floatValue());
        refund.setRefundReason(request.getRefundReason());
        refund.setOrderNum(order.getOrderNum());
        refund.setOrderItemNum(targetItem.getId());
        refund.setGoodsId(targetItem.getGoodsId());
        refund.setGoodsSkuId(targetItem.getGoodsSkuId());
        refund.setGoodsName(targetItem.getGoodsTitle());
        refund.setGoodsSku(targetItem.getGoodsSpec());
        refund.setHasGoodReturn(request.getHasGoodReturn());
        refund.setQuantity((long) request.getQuantity());
        refund.setRemark(request.getRemark());
        refund.setStatus(10090);
        refund.setErpStatus(0);
        refund.setCreateTime(now);
        refund.setCreateBy(username);
        refund.setMerchantId(order.getMerchantId());
        refundService.save(refund);

        // 更新订单项退款状态
        OOrderItem updateItem = new OOrderItem();
        updateItem.setId(targetItem.getId());
        int newRefundCount = (targetItem.getRefundCount() != null ? targetItem.getRefundCount() : 0) + request.getQuantity();
        updateItem.setRefundCount(newRefundCount);
        updateItem.setRefundStatus(newRefundCount >= targetItem.getQuantity() ? 4 : 3);
        updateItem.setUpdateBy(username);
        updateItem.setUpdateTime(now);
        orderService.updateOrderItem(updateItem);

        // 解锁库存
        OGoodsInventory inv = goodsInventoryService.getOne(
            new LambdaQueryWrapper<OGoodsInventory>().eq(OGoodsInventory::getSkuId, targetItem.getGoodsSkuId()));
        if (inv != null) {
            goodsInventoryService.lockStock(inv.getId(), -request.getQuantity());
        }

        return AjaxResult.success(refund.getId());
    }

    @Data
    public static class RefundRequest {
        private Long orderId;
        private Long orderItemId;
        private Integer quantity;
        private Double refundFee;
        private String refundReason;
        private Integer hasGoodReturn;
        private String remark;
    }
}
