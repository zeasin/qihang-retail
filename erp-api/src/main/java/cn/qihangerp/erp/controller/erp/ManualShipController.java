package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.request.ManualShipSearchRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ManualShipService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/manual/ship")
public class ManualShipController extends BaseController {

    private final ManualShipService manualShipService;

    @GetMapping("/list")
    public TableDataInfo list(ManualShipSearchRequest query, PageQuery pageQuery) {
        var pageList = manualShipService.queryPickupOrders(query, pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable String id) {
        return success(manualShipService.getOrderDetail(id));
    }

    @GetMapping("/stats")
    public AjaxResult stats() {
        return AjaxResult.success(manualShipService.getStats());
    }

    @PostMapping("/confirm/{orderId}")
    public AjaxResult confirmPickup(@PathVariable String orderId) {
        var result = manualShipService.confirmPickup(orderId, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    @PostMapping("/batchConfirm")
    public AjaxResult batchConfirmPickup(@RequestBody List<String> orderIds) {
        var result = manualShipService.batchConfirmPickup(orderIds, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }
}
