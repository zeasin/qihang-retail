package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.*;
import cn.qihangerp.model.entity.OOrder;
import cn.qihangerp.model.entity.OOrderItem;
import cn.qihangerp.request.OrderSearchRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OOrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 线下销售订单Controller
 *
 * @author qihang
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/sale/order")
public class SaleOrderController extends BaseController {

    private final OOrderService orderService;

    /**
     * 查询销售订单列表
     */
    @GetMapping("/list")
    public TableDataInfo list(OrderSearchRequest bo, PageQuery pageQuery) {
        var pageList = orderService.querySaleOrderPageList(bo, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 获取销售订单详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(orderService.queryDetailById(id));
    }

    /**
     * 新增销售订单
     */
    @PostMapping
    public AjaxResult add(@RequestBody OOrder order) {
        List<OOrderItem> itemList = order.getItemList();
        ResultVo<Long> result = orderService.saveSaleOrder(order, itemList, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success(result.getData());
    }

    /**
     * 修改销售订单
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OOrder order) {
        List<OOrderItem> itemList = order.getItemList();
        ResultVo<Long> result = orderService.updateSaleOrder(order, itemList, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    /**
     * 删除销售订单
     */
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        ResultVo<Long> result = orderService.removeSaleOrder(id);
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }
}
