package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.request.AfterSaleApplyRequest;
import cn.qihangerp.model.request.AfterSaleAuditRequest;
import cn.qihangerp.model.request.AfterSaleProcessRequest;
import cn.qihangerp.model.request.AfterSaleSearchRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.service.ORefundService;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.entity.OOrderItem;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 售后台账 Controller
 * 线下零售售后处理：发起售后 → 审核 → 退货收货 → 退款/换货 → 完成
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/afterSale")
public class AfterSaleController extends BaseController {

    private final ORefundService refundService;
    private final OOrderService orderService;

    /**
     * 售后台账列表
     */
    @GetMapping("/list")
    public TableDataInfo list(AfterSaleSearchRequest query, PageQuery pageQuery) {
        var pageList = refundService.queryAfterSalePageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 售后详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(refundService.getAfterSaleDetail(id));
    }

    /**
     * 发起售后申请
     */
    @PostMapping("/apply")
    public AjaxResult apply(@RequestBody AfterSaleApplyRequest request) {
        var result = refundService.createAfterSale(request, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success(result.getData());
    }

    /**
     * 审核售后（通过/拒绝）
     */
    @PutMapping("/audit/{id}")
    public AjaxResult audit(@PathVariable Long id, @RequestBody AfterSaleAuditRequest request) {
        var result = refundService.auditAfterSale(id, request, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    /**
     * 退货收货（顾客退回商品已收到）
     */
    @PutMapping("/receive/{id}")
    public AjaxResult receive(@PathVariable Long id) {
        var result = refundService.receiveReturnGoods(id, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    /**
     * 执行退款
     */
    @PutMapping("/process/{id}")
    public AjaxResult process(@PathVariable Long id, @RequestBody AfterSaleProcessRequest request) {
        var result = refundService.processAfterSale(id, request, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    /**
     * 换货发货
     */
    @PutMapping("/shipExchange/{id}")
    public AjaxResult shipExchange(@PathVariable Long id) {
        var result = refundService.shipExchange(id, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    /**
     * 取消售后
     */
    @PutMapping("/cancel/{id}")
    public AjaxResult cancel(@PathVariable Long id) {
        var result = refundService.cancelAfterSale(id, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    /**
     * 台账统计
     */
    @GetMapping("/stats")
    public AjaxResult stats() {
        Map<String, Object> stats = refundService.getAfterSaleStats();
        return AjaxResult.success(stats);
    }

    /**
     * 查询可发起售后的订单列表（已完成的POS订单）
     */
    @GetMapping("/refundableOrders")
    public TableDataInfo refundableOrders(OOrder query, PageQuery pageQuery) {
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OOrder>();
        wrapper.eq(OOrder::getOrderSource, "POS");
        wrapper.eq(OOrder::getOrderStatus, 3);
        if (query.getShopId() != null) {
            wrapper.eq(OOrder::getShopId, query.getShopId());
        }
        if (query.getOrderNum() != null && !query.getOrderNum().isEmpty()) {
            wrapper.like(OOrder::getOrderNum, query.getOrderNum());
        }
        wrapper.orderByDesc(OOrder::getCreateTime);
        var page = orderService.page(pageQuery.build(), wrapper);
        List<OOrder> records = page.getRecords();
        for (OOrder order : records) {
            List<OOrderItem> items = orderService.selectItemsByOrderId(order.getId());
            order.setItemList(items);
        }
        return getDataTable(cn.qihangerp.common.PageResult.build(page));
    }

    /**
     * 获取售后配置（授权期、审核阈值、退货原因）
     */
    @GetMapping("/config")
    public AjaxResult getConfig() {
        return success(refundService.getAfterSaleConfig());
    }
}
