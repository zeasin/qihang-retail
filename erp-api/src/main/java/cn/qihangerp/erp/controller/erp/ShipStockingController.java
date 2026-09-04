package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.request.ShipDeliveryRequest;
import cn.qihangerp.model.request.ShipStockingSearchRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ShipStockingService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单备货/配送 Controller
 */
@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/ship/stocking")
public class ShipStockingController extends BaseController {

    private final ShipStockingService shipStockingService;

    @GetMapping("/list")
    public TableDataInfo list(ShipStockingSearchRequest query, PageQuery pageQuery) {
        var pageList = shipStockingService.queryPendingList(query, pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable String id) {
        return success(shipStockingService.getOrderDetail(id));
    }

    @PostMapping("/deliver")
    public AjaxResult deliver(@RequestBody ShipDeliveryRequest request) {
        var result = shipStockingService.executeDelivery(request, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    @PostMapping("/pickup/{orderId}")
    public AjaxResult pickup(@PathVariable String orderId) {
        var result = shipStockingService.executePickup(orderId, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    @GetMapping("/stats")
    public AjaxResult stats() {
        return AjaxResult.success(shipStockingService.getStats());
    }
}
