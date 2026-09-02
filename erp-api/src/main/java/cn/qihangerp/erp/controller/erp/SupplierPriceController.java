package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.bo.SupplierPriceSaveBo;
import cn.qihangerp.model.entity.ErpSupplierGoodsPrice;
import cn.qihangerp.model.entity.ErpSupplierProductItem;
import cn.qihangerp.mapper.ErpSupplierProductItemMapper;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpSupplierGoodsPriceService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 供应商报价管理
 */
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/supplier/price")
public class SupplierPriceController extends BaseController {

    private final ErpSupplierGoodsPriceService supplierGoodsPriceService;
    private final ErpSupplierProductItemMapper supplierProductItemMapper;

    @GetMapping("/list")
    public TableDataInfo list(ErpSupplierGoodsPrice query, PageQuery pageQuery) {
        var pageList = supplierGoodsPriceService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(supplierGoodsPriceService.getById(id));
    }

    @PostMapping("/add")
    public AjaxResult add(@RequestBody ErpSupplierGoodsPrice bo) {
        bo.setCreateBy(getUsername());
        bo.setMerchantId(0L);
        bo.setStatus(1);
        return toAjax(supplierGoodsPriceService.save(bo));
    }

    @PutMapping("/edit")
    public AjaxResult edit(@RequestBody ErpSupplierGoodsPrice bo) {
        bo.setUpdateBy(getUsername());
        return toAjax(supplierGoodsPriceService.updateById(bo));
    }

    @DeleteMapping("/del/{id}")
    public AjaxResult remove(@PathVariable Long id) {
        return toAjax(supplierGoodsPriceService.removeById(id));
    }

    @PostMapping("/savePrice")
    public AjaxResult savePrice(@RequestBody SupplierPriceSaveBo bo) {
        if (bo.getSupplierId() == null) return error("供应商ID不能为空");
        if (bo.getSkus() == null || bo.getSkus().isEmpty()) return error("请至少设置一个SKU价格");

        for (var sku : bo.getSkus()) {
            if (sku.getSkuItemId() == null && sku.getErpSkuId() == null) continue;

            LambdaQueryWrapper<ErpSupplierProductItem> qw = new LambdaQueryWrapper<>();
            qw.eq(ErpSupplierProductItem::getSupplierId, bo.getSupplierId());
            if (sku.getSkuItemId() != null) {
                qw.eq(ErpSupplierProductItem::getId, sku.getSkuItemId());
            } else {
                qw.eq(ErpSupplierProductItem::getErpGoodsSkuId, sku.getErpSkuId());
            }
            ErpSupplierProductItem item = supplierProductItemMapper.selectOne(qw);
            if (item == null) continue;

            ErpSupplierProductItem update = new ErpSupplierProductItem();
            update.setId(item.getId());
            update.setPrice(sku.getPrice());
            update.setUpdateBy(getUsername());
            update.setUpdateTime(LocalDateTime.now());
            supplierProductItemMapper.updateById(update);

            ErpSupplierGoodsPrice priceRecord = new ErpSupplierGoodsPrice();
            priceRecord.setSupplierId(bo.getSupplierId());
            priceRecord.setSupplierProductId(item.getSupplierProductId());
            priceRecord.setSupplierProductItemId(item.getId());
            priceRecord.setSkuCode(item.getSkuCode());
            priceRecord.setPrice(sku.getPrice());
            priceRecord.setMerchantId(0L);
            priceRecord.setStatus(1);
            priceRecord.setCreateBy(getUsername());
            priceRecord.setCreateTime(LocalDateTime.now());
            supplierGoodsPriceService.save(priceRecord);
        }
        return success();
    }
}
