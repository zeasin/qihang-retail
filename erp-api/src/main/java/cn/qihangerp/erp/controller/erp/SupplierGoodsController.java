package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.bo.SupplierProductAddBo;
import cn.qihangerp.model.bo.SupplierGoodsLinkBo;
import cn.qihangerp.model.entity.ErpSupplierProduct;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpSupplierProductService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商商品管理 - SPU维度
 */
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/supplier/goods")
public class SupplierGoodsController extends BaseController {

    private final ErpSupplierProductService supplierProductService;

    @GetMapping("/list")
    public TableDataInfo list(ErpSupplierProduct query, PageQuery pageQuery) {
        var pageList = supplierProductService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        ErpSupplierProduct product = supplierProductService.getById(id);
        if (product == null) {
            return error("供应商商品不存在");
        }
        product.setSkuList(supplierProductService.queryItemListByProductId(id));
        return success(product);
    }

    @PostMapping("/add")
    public AjaxResult add(@RequestBody SupplierProductAddBo bo) {
        var result = supplierProductService.addProduct(getUsername(), bo);
        return result.getCode() == 200 ? success(result.getData()) : error(result.getMsg());
    }

    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody SupplierProductAddBo bo) {
        var result = supplierProductService.updateProduct(getUsername(), bo);
        return result.getCode() == 200 ? success() : error(result.getMsg());
    }

    @DeleteMapping("/del/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        supplierProductService.deleteProduct(id);
        return success();
    }

    @PutMapping("/status")
    public AjaxResult updateStatus(@RequestBody ErpSupplierProduct bo) {
        supplierProductService.updateStatus(bo.getId(), bo.getStatus());
        return success();
    }

    /**
     * 从商品库关联商品到供应商
     */
    @PostMapping("/link")
    public AjaxResult linkGoods(@RequestBody SupplierGoodsLinkBo bo) {
        var result = supplierProductService.linkGoodsFromLibrary(getUsername(), bo);
        return result.getCode() == 0 ? success() : error(result.getMsg());
    }
}
