package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpStockOut;
import cn.qihangerp.model.request.StockOutCreateRequest;
import cn.qihangerp.model.request.StockOutItemRequest;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpStockOutService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@RestController
@RequestMapping("/api/erp-api/stockOut")
public class StockOutController extends BaseController {
    private final ErpStockOutService stockOutService;

    @GetMapping("/list")
    public TableDataInfo list(ErpStockOut bo, PageQuery pageQuery)
    {
        var pageList = stockOutService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }



    @PostMapping("/create")
    public AjaxResult createEntry(@RequestBody StockOutCreateRequest request)
    {
        ResultVo<Long> resultVo = stockOutService.createEntry(getUserId(), getUsername(), request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        ErpStockOut entry = stockOutService.getDetailAndItemById(id);
        return success(entry);
    }
    @GetMapping(value = "/print/{id}")
    public AjaxResult print(@PathVariable("id") Long id)
    {
        ErpStockOut out = new ErpStockOut();
        out.setId(id);
        out.setPrintStatus(1);
        out.setPrintTime(LocalDateTime.now());
        stockOutService.updateById(out);
        return AjaxResult.success();
    }
    @PostMapping("/out")
    public AjaxResult out(@RequestBody StockOutItemRequest request)
    {
        ResultVo<Long> resultVo = stockOutService.stockOut(getUserId(), getUsername(), request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }
}
