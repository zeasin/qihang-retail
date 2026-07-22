package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpSalesPerson;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpSalesPersonService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 营业员管理Controller（POS收银用）
 * 复用 erp_sales_person 表
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/employee")
public class PosEmployeeController extends BaseController {

    private final ErpSalesPersonService salesPersonService;

    /**
     * 查询营业员列表
     */
    @GetMapping("/list")
    public TableDataInfo list(ErpSalesPerson query, PageQuery pageQuery) {
        PageResult<ErpSalesPerson> pageList = salesPersonService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 获取营业员详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(salesPersonService.getById(id));
    }

    /**
     * 新增营业员
     */
    @PostMapping
    public AjaxResult add(@RequestBody ErpSalesPerson employee) {
        return toAjax(salesPersonService.save(employee));
    }

    /**
     * 修改营业员
     */
    @PutMapping
    public AjaxResult edit(@RequestBody ErpSalesPerson employee) {
        return toAjax(salesPersonService.updateById(employee));
    }

    /**
     * 删除营业员
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(salesPersonService.removeByIds(java.util.Arrays.asList(ids)));
    }

    /**
     * 获取当前门店的营业员（下拉选择用）
     */
    @GetMapping("/options")
    public AjaxResult options(@RequestParam Long shopId) {
        ErpSalesPerson query = new ErpSalesPerson();
        query.setShopId(shopId);
        List<ErpSalesPerson> list = salesPersonService.queryList(query);
        return success(list);
    }
}
