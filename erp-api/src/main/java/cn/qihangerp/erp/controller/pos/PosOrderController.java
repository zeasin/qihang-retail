package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.request.OrderSearchRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OOrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * POS订单查询Controller
 * 数据源：o_order（order_source = POS）
 */
@AllArgsConstructor
@RestController
@RequestMapping("/pos-api/order")
public class PosOrderController extends BaseController {

    private final OOrderService orderService;

    /**
     * 查询POS订单列表（仅 order_source = POS）
     */
    @GetMapping("/list")
    public TableDataInfo list(OrderSearchRequest bo, PageQuery pageQuery) {
        bo.setOrderSource("POS");
        PageResult<OOrder> pageList = orderService.queryPageList(bo, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 查询POS订单详情（含明细）
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        OOrder order = orderService.queryDetailById(id);
        if (order == null) {
            return AjaxResult.error("订单不存在");
        }
        return success(order);
    }

    /**
     * 查询今日销售统计
     */
    @GetMapping("/today")
    public AjaxResult todayStats(@RequestParam Long shopId) {
        // TODO: 实现今日销售统计（基于 o_order order_source=POS）
        return success("今日统计功能待实现");
    }

    /**
     * 查询销售日报
     */
    @GetMapping("/daily")
    public AjaxResult dailyReport(@RequestParam Long shopId, @RequestParam String date) {
        // TODO: 实现销售日报（基于 o_order order_source=POS）
        return success("日报功能待实现");
    }
}
