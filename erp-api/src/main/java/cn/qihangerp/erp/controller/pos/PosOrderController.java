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
 * 销售单查询Controller（POS收银用）
 * 复用 erp_sales_order + erp_sales_order_item 表
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/order")
public class PosOrderController extends BaseController {

    private final ErpSalesOrderService salesOrderService;

    /**
     * 查询销售单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(ErpSalesOrder query, PageQuery pageQuery) {
        PageResult<ErpSalesOrder> pageList = salesOrderService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 查询销售单详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        ErpSalesOrder order = salesOrderService.getById(id);
        if (order == null) {
            return AjaxResult.error("销售单不存在");
        }
        List<ErpSalesOrderItem> items = salesOrderService.selectItemsByOrderId(id);
        order.setItems(items);
        return success(order);
    }

    /**
     * 查询今日销售统计
     */
    @GetMapping("/today")
    public AjaxResult todayStats(@RequestParam Long shopId) {
        // TODO: 实现今日销售统计
        // 总销售额、总订单数、总会员数
        return success("今日统计功能待实现");
    }

    /**
     * 查询销售日报
     */
    @GetMapping("/daily")
    public AjaxResult dailyReport(@RequestParam Long shopId, @RequestParam String date) {
        // TODO: 实现销售日报
        return success("日报功能待实现");
    }
}
