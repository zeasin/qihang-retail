package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpSalesOrder;
import cn.qihangerp.model.entity.ErpSalesOrderItem;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpSalesOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POS收银Controller
 * 复用 erp_sales_order + erp_sales_order_item 表
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/cashier")
public class PosCashierController extends BaseController {

    private final ErpSalesOrderService salesOrderService;

    /**
     * 提交销售单（收银结算）
     */
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody SalesOrderRequest request) {
        // TODO: 实现销售单提交逻辑
        // 1. 创建销售单 erp_sales_order
        // 2. 创建销售明细 erp_sales_order_item
        // 3. 扣减库存
        // 4. 会员积分累计
        // 5. 优惠券核销
        return success("销售单提交功能待实现");
    }

    /**
     * 查询销售单列表
     */
    @GetMapping("/order/list")
    public TableDataInfo orderList(ErpSalesOrder query, PageQuery pageQuery) {
        PageResult<ErpSalesOrder> pageList = salesOrderService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 查询销售单详情
     */
    @GetMapping("/order/{id}")
    public AjaxResult getOrderInfo(@PathVariable("id") Long id) {
        ErpSalesOrder order = salesOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("销售单不存在");
        }
        List<ErpSalesOrderItem> items = salesOrderService.selectItemsByOrderId(id);
        order.setItems(items);
        return success(order);
    }

    /**
     * 退货
     */
    @PostMapping("/refund")
    public AjaxResult refund(@RequestBody RefundRequest request) {
        // TODO: 实现退货逻辑
        // 1. 验证原订单
        // 2. 创建退货单
        // 3. 恢复库存
        // 4. 退款处理
        return success("退货功能待实现");
    }

    /**
     * 销售单请求
     */
    @lombok.Data
    public static class SalesOrderRequest {
        private Long shopId;
        private Long memberId;
        private Long salesmanId;
        private String payMethod; // CASH/WECHAT/ALIPAY/BANK_CARD/MEMBER_BALANCE
        private java.math.BigDecimal totalAmount;
        private java.math.BigDecimal discountAmount;
        private java.math.BigDecimal paidAmount;
        private List<OrderItemRequest> items;
        private String remark;
    }

    /**
     * 订单明细请求
     */
    @lombok.Data
    public static class OrderItemRequest {
        private Long goodsId;
        private Long skuId;
        private String goodsName;
        private java.math.BigDecimal price;
        private Integer quantity;
        private java.math.BigDecimal subtotal;
    }

    /**
     * 退货请求
     */
    @lombok.Data
    public static class RefundRequest {
        private Long orderId;
        private String refundReason;
        private java.math.BigDecimal refundAmount;
        private String refundMethod; // ORIGINAL/CASH/BALANCE
    }
}
