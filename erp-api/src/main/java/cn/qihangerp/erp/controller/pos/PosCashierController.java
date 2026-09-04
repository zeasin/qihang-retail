package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/pos-api/cashier")
public class PosCashierController extends BaseController {

    private final OOrderService orderService;
    private final OGoodsInventoryService goodsInventoryService;
    private final ErpStockOutService stockOutService;
    private final ErpStockOutItemService stockOutItemService;

    /**
     * POS收银提交订单 → 锁定库存
     */
    @PostMapping("/submit")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult submit(@RequestBody PosOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return AjaxResult.error("请添加商品");
        }

        String username = getUsername();
        LocalDateTime now = LocalDateTime.now();

        // 下单前校验：可用库存必须大于等于下单数量
        for (PosOrderItemRequest item : request.getItems()) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                return AjaxResult.error("商品规格或数量不合法");
            }
            OGoodsInventory inv = goodsInventoryService.getOne(
                new LambdaQueryWrapper<OGoodsInventory>().eq(OGoodsInventory::getSkuId, item.getSkuId()));
            if (inv == null) {
                return AjaxResult.error("SKU[" + item.getSkuId() + "]库存记录不存在");
            }
            if (inv.getAvailableQuantity() < item.getQuantity()) {
                return AjaxResult.error("商品[" + item.getName() + "]可用库存不足（剩余" + inv.getAvailableQuantity() + "）");
            }
        }

        OOrder order = new OOrder();
        order.setOrderNum(request.getOrderNo());
        order.setOrderSource("POS");
        order.setOrderMode(1);
        order.setShopType(0);
        order.setShopId(0L);
        order.setMerchantId(0L);
        order.setOrderStatus(0);
        order.setShipStatus(0);
        order.setDistStatus(0);
        order.setHasGift(0);
        order.setDeliveryMethod(2);
        order.setOrderTime(now);
        order.setGoodsAmount(request.getPayAmount());
        order.setAmount(request.getPayAmount());
        order.setPayment(request.getPayAmount());
        order.setCreateBy(username);
        order.setCreateTime(now);
        orderService.save(order);

        String orderId = order.getId();

        for (PosOrderItemRequest item : request.getItems()) {
            OOrderItem orderItem = new OOrderItem();
            orderItem.setOrderId(orderId);
            orderItem.setOrderNum(request.getOrderNo());
            orderItem.setSubOrderNum(request.getOrderNo());
            orderItem.setGoodsId(item.getGoodsId());
            orderItem.setGoodsSkuId(item.getSkuId());
            orderItem.setGoodsTitle(item.getName());
            orderItem.setBarcode(item.getBarCode());
            orderItem.setGoodsPrice(item.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setItemAmount(item.getPrice() * item.getQuantity());
            orderItem.setRefundStatus(1);
            orderItem.setShipStatus(0);
            orderItem.setHasPushErp(0);
            orderItem.setIsGift(0);
            orderItem.setShopId(order.getShopId());
            orderItem.setShopType(order.getShopType());
            orderItem.setMerchantId(order.getMerchantId());
            orderItem.setCreateBy(username);
            orderItem.setCreateTime(now);
            orderService.insertOrderItem(orderItem);

            // 锁定库存，校验锁定数量与下单数量是否匹配
            if (!lockInventory(item.getSkuId(), item.getQuantity())) {
                throw new RuntimeException("SKU[" + item.getSkuId() + "]库存不足，锁定失败");
            }
        }

        return AjaxResult.success(order.getId());
    }

    /**
     * 取消订单 → 解锁库存
     */
    @PostMapping("/cancel")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult cancel(@RequestBody CancelOrderRequest request) {
        if (request.getOrderId() == null) return AjaxResult.error("订单ID不能为空");

        OOrder order = orderService.getById(request.getOrderId());
        if (order == null) return AjaxResult.error("订单不存在");
        if (order.getOrderStatus() != 0) return AjaxResult.error("订单状态不可取消");

        String username = getUsername();
        LocalDateTime now = LocalDateTime.now();

        List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
        for (OOrderItem item : items) {
            unlockInventory(item.getGoodsSkuId(), item.getQuantity());
        }

        OOrder update = new OOrder();
        update.setId(order.getId());
        update.setOrderStatus(11);
        update.setCancelReason(request.getReason());
        update.setUpdateBy(username);
        update.setUpdateTime(now);
        orderService.updateById(update);

        return AjaxResult.success();
    }

    /**
     * 确认订单 → 生成出库单（type=5 表示锁库出库，出库时从锁定库存转实扣）
     */
    @PostMapping("/confirm")
    @Transactional(rollbackFor = Exception.class)
    public AjaxResult confirm(@RequestBody ConfirmOrderRequest request) {
        if (request.getOrderId() == null) return AjaxResult.error("订单ID不能为空");

        OOrder order = orderService.getById(request.getOrderId());
        if (order == null) return AjaxResult.error("订单不存在");
        if (order.getOrderStatus() != 0) return AjaxResult.error("订单状态不可确认");

        String username = getUsername();
        LocalDateTime now = LocalDateTime.now();

        List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());

        // 1. 创建出库单
        ErpStockOut stockOut = new ErpStockOut();
        stockOut.setOutNum("SO-" + request.getOrderId());
        stockOut.setSourceNum(order.getOrderNum());
        stockOut.setSourceId(Long.parseLong(order.getId()));
        stockOut.setType(5); // 5=POS锁库出库
        stockOut.setGoodsUnit(items.size());
        int totalQty = items.stream().mapToInt(OOrderItem::getQuantity).sum();
        stockOut.setSpecUnit(items.size());
        stockOut.setSpecUnitTotal(totalQty);
        stockOut.setOutTotal(0);
        stockOut.setStatus(0); // 待出库
        stockOut.setCreateBy(username);
        stockOut.setCreateTime(now);
        stockOutService.save(stockOut);

        // 2. 创建出库单明细
        for (OOrderItem item : items) {
            ErpStockOutItem outItem = new ErpStockOutItem();
            outItem.setEntryId(stockOut.getId());
            outItem.setType(5);
            outItem.setSourceOrderId(Long.parseLong(order.getId()));
            outItem.setSourceOrderNum(order.getOrderNum());
            outItem.setSourceOrderItemId(Long.parseLong(item.getId()));
            outItem.setGoodsId(item.getGoodsId());
            outItem.setGoodsName(item.getGoodsTitle());
            outItem.setSkuId(item.getGoodsSkuId());
            outItem.setOriginalQuantity(item.getQuantity());
            outItem.setOutQuantity(0);
            outItem.setStatus(0);
            outItem.setCreateBy(username);
            outItem.setCreateTime(now);
            stockOutItemService.save(outItem);
        }

        // 3. 更新订单状态为已确认
        OOrder updateOrder = new OOrder();
        updateOrder.setId(order.getId());
        updateOrder.setOrderStatus(3);
        updateOrder.setShipStatus(2);
        updateOrder.setUpdateBy(username);
        updateOrder.setUpdateTime(now);
        orderService.updateById(updateOrder);

        return AjaxResult.success(stockOut.getId());
    }

    private boolean lockInventory(Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) return false;
        OGoodsInventory inv = goodsInventoryService.getOne(
            new LambdaQueryWrapper<OGoodsInventory>().eq(OGoodsInventory::getSkuId, skuId));
        if (inv == null) return false;
        return goodsInventoryService.lockStock(inv.getId(), quantity);
    }

    private void unlockInventory(Long skuId, int quantity) {
        if (skuId == null || quantity <= 0) return;
        OGoodsInventory inv = goodsInventoryService.getOne(
            new LambdaQueryWrapper<OGoodsInventory>().eq(OGoodsInventory::getSkuId, skuId));
        if (inv != null) goodsInventoryService.lockStock(inv.getId(), -quantity);
    }

    @GetMapping("/order/list")
    public TableDataInfo orderList(OOrder query, PageQuery pageQuery) {
        query.setOrderSource("POS");
        PageResult<OOrder> pageList = orderService.queryPageListBySource(query, pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/order/{id}")
    public AjaxResult getOrderInfo(@PathVariable("id") String id) {
        OOrder order = orderService.getById(id);
        if (order == null) return AjaxResult.error("订单不存在");
        List<OOrderItem> items = orderService.selectItemsByOrderId(id);
        order.setItemList(items);
        return success(order);
    }

    @Data
    public static class PosOrderRequest {
        private String orderNo;
        private Double payAmount;
        private String payMethod;
        private List<PosOrderItemRequest> items;
    }

    @Data
    public static class PosOrderItemRequest {
        private Long goodsId;
        private Long skuId;
        private String name;
        private String skuName;
        private Double price;
        private Integer quantity;
        private String barCode;
    }

    @Data
    public static class CancelOrderRequest {
        private String orderId;
        private String reason;
    }

    @Data
    public static class ConfirmOrderRequest {
        private String orderId;
    }
}
