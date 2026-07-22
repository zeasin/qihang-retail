package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpStockIn;
import cn.qihangerp.model.request.StockInCreateRequest;
import cn.qihangerp.model.request.StockInRequest;

import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpStockInService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/api/erp-api/stockIn")
public class StockInController extends BaseController {
    private final ErpStockInService stockInService;
    @GetMapping("/list")
    public TableDataInfo list(ErpStockIn bo, PageQuery pageQuery)
    {
        var pageList = stockInService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }

    @PostMapping("/create")
    public AjaxResult createEntry(@RequestBody StockInCreateRequest request)
    {
        ResultVo<Long> resultVo = stockInService.createEntry(getUserId(), getUsername(), request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    @PostMapping("/in")
    public AjaxResult in(@RequestBody StockInRequest request)
    {
        ResultVo<Long> resultVo = stockInService.stockIn(getUserId(), getUsername(), request);
        if(resultVo.getCode()==0)
            return AjaxResult.success();
        else return AjaxResult.error(resultVo.getMsg());
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        ErpStockIn entry = stockInService.getDetailAndItemById(id);

        return success(entry);
    }

}
