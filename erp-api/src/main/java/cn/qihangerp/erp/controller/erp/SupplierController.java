package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpSupplier;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpSupplierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@AllArgsConstructor
@RestController
@RequestMapping("/api/erp-api/supplier")
public class SupplierController extends BaseController {
    private final ErpSupplierService supplierService;

    @GetMapping("/list_all")
    public TableDataInfo list_all(ErpSupplier bo, PageQuery pageQuery) {
        var pageList = supplierService.list(new LambdaQueryWrapper<ErpSupplier>()
                .eq(ErpSupplier::getIsDelete,0)
                .eq(bo.getIsShipper()!=null,ErpSupplier::getIsShipper,bo.getIsShipper())
        );
        return getDataTable(pageList);
    }

    @GetMapping("/list")
    public TableDataInfo list(ErpSupplier bo, PageQuery pageQuery) {
        var pageList = supplierService.queryPageList(bo,pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(supplierService.getById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody ErpSupplier scmSupplier) {
        return toAjax(supplierService.save(scmSupplier));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody ErpSupplier scmSupplier) {
        return toAjax(supplierService.updateById(scmSupplier));
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(supplierService.removeByIds(Arrays.stream(ids).toList()));
    }
}
