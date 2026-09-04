package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.request.RiderDeliverySearchRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.RiderDeliveryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/rider/delivery")
public class RiderDeliveryController extends BaseController {

    private final RiderDeliveryService riderDeliveryService;

    @GetMapping("/list")
    public TableDataInfo list(RiderDeliverySearchRequest query, PageQuery pageQuery) {
        var pageList = riderDeliveryService.queryRiderOrders(query, pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable String id) {
        return success(riderDeliveryService.getOrderDetail(id));
    }

    @GetMapping("/stats")
    public AjaxResult stats() {
        return AjaxResult.success(riderDeliveryService.getStats());
    }

    @PostMapping("/batchPrinted")
    public AjaxResult batchPrinted(@RequestBody List<String> orderIds) {
        var result = riderDeliveryService.batchPrinted(orderIds, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }

    @PostMapping("/batchShip")
    public AjaxResult batchShip(@RequestBody List<String> orderIds) {
        var result = riderDeliveryService.batchShip(orderIds, getUsername());
        if (result.getCode() != 0) {
            return AjaxResult.error(result.getMsg());
        }
        return AjaxResult.success();
    }
}
