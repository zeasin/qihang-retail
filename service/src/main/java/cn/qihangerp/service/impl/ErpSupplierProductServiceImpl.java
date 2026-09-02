package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.enums.EnumWarehouseType;
import cn.qihangerp.model.bo.SupplierProductAddBo;
import cn.qihangerp.model.bo.SupplierGoodsLinkBo;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.mapper.*;
import cn.qihangerp.service.ErpSupplierProductService;
import cn.qihangerp.service.ErpSupplierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ErpSupplierProductServiceImpl extends ServiceImpl<ErpSupplierProductMapper, ErpSupplierProduct>
    implements ErpSupplierProductService {

    private final ErpSupplierProductItemMapper itemMapper;
    private final ErpWarehouseMapper warehouseMapper;
    private final ErpSupplierService supplierService;
    private final OGoodsMapper goodsMapper;
    private final OGoodsSkuMapper goodsSkuMapper;
    private final ErpSupplierGoodsPriceMapper supplierGoodsPriceMapper;

    @Override
    public PageResult<ErpSupplierProduct> queryPageList(ErpSupplierProduct goods, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpSupplierProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(goods.getSupplierId() != null, ErpSupplierProduct::getSupplierId, goods.getSupplierId());
        queryWrapper.eq(goods.getStatus() != null, ErpSupplierProduct::getStatus, goods.getStatus());
        queryWrapper.like(StringUtils.hasText(goods.getProductName()), ErpSupplierProduct::getProductName, goods.getProductName());
        queryWrapper.like(StringUtils.hasText(goods.getProductNum()), ErpSupplierProduct::getProductNum, goods.getProductNum());
        queryWrapper.eq(goods.getCategoryId() != null, ErpSupplierProduct::getCategoryId, goods.getCategoryId());

        Page<ErpSupplierProduct> page = pageQuery.build();
        this.page(page, queryWrapper);

        if (page.getRecords() != null && !page.getRecords().isEmpty()) {
            for (ErpSupplierProduct product : page.getRecords()) {
                Long skuCount = itemMapper.selectCount(new LambdaQueryWrapper<ErpSupplierProductItem>()
                        .eq(ErpSupplierProductItem::getSupplierProductId, product.getId()));
                product.setSkuCount(skuCount.intValue());

                if (product.getSupplierId() != null) {
                    var supplier = supplierService.getById(product.getSupplierId());
                    if (supplier != null) {
                        product.setSupplierName(supplier.getName());
                    }
                }
            }
        }

        return PageResult.build(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo<Long> addProduct(String username, SupplierProductAddBo bo) {
        ErpSupplier supplier = supplierService.getById(bo.getSupplierId());
        if (supplier == null) {
            return ResultVo.error(500, "供应商不存在");
        }

        ErpSupplierProduct product = new ErpSupplierProduct();
        product.setSupplierId(bo.getSupplierId());
        product.setProductName(bo.getProductName());
        product.setImageUrl(bo.getImageUrl());
        product.setProductNum(bo.getProductNum());
        product.setCategoryId(bo.getCategoryId());
        product.setBrandId(bo.getBrandId());
        product.setUnitName(bo.getUnitName());
        product.setLength(bo.getLength());
        product.setWidth(bo.getWidth());
        product.setHeight(bo.getHeight());
        product.setWeight(bo.getWeight());
        product.setStatus(1);
        product.setRemark(bo.getRemark());
        product.setCreateBy(username);
        this.save(product);

        if (bo.getItemList() != null && !bo.getItemList().isEmpty()) {
            for (SupplierProductAddBo.SupplierProductItemBo itemBo : bo.getItemList()) {
                ErpSupplierProductItem item = new ErpSupplierProductItem();
                item.setSupplierProductId(product.getId());
                item.setSupplierId(bo.getSupplierId());
                item.setSkuCode(itemBo.getSkuCode());
                item.setProductName(bo.getProductName());
                item.setBarCode(itemBo.getBarCode());
                item.setColorId(itemBo.getColorId());
                item.setColorValue(itemBo.getColorValue());
                item.setColorImage(itemBo.getColorImage());
                item.setSizeId(itemBo.getSizeId());
                item.setSizeValue(itemBo.getSizeValue());
                item.setStyleId(itemBo.getStyleId());
                item.setStyleValue(itemBo.getStyleValue());
                item.setStandard(itemBo.getStandard());
                item.setBrandNo(itemBo.getBrandNo());
                item.setBrandName(itemBo.getBrandName());
                item.setPrice(itemBo.getPrice());
                item.setStatus(1);
                item.setCreateBy(username);
                itemMapper.insert(item);
            }
        }

        return ResultVo.success(product.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo updateProduct(String username, SupplierProductAddBo bo) {
        if (bo.getId() == null) {
            return ResultVo.error(500, "商品ID不能为空");
        }

        ErpSupplierProduct product = this.getById(bo.getId());
        if (product == null) {
            return ResultVo.error(500, "商品不存在");
        }

        product.setProductName(bo.getProductName());
        product.setImageUrl(bo.getImageUrl());
        product.setProductNum(bo.getProductNum());
        product.setCategoryId(bo.getCategoryId());
        product.setBrandId(bo.getBrandId());
        product.setUnitName(bo.getUnitName());
        product.setLength(bo.getLength());
        product.setWidth(bo.getWidth());
        product.setHeight(bo.getHeight());
        product.setWeight(bo.getWeight());
        product.setRemark(bo.getRemark());
        product.setUpdateBy(username);
        this.updateById(product);

        List<ErpSupplierProductItem> existingItems = queryItemListByProductId(bo.getId());
        java.util.Map<Long, ErpSupplierProductItem> existingMap = new java.util.HashMap<>();
        for (ErpSupplierProductItem item : existingItems) {
            existingMap.put(item.getId(), item);
        }

        if (bo.getItemList() != null) {
            for (SupplierProductAddBo.SupplierProductItemBo itemBo : bo.getItemList()) {
                ErpSupplierProductItem item;
                if (itemBo.getId() != null && existingMap.containsKey(itemBo.getId())) {
                    item = existingMap.get(itemBo.getId());
                    item.setSkuCode(itemBo.getSkuCode());
                    item.setBarCode(itemBo.getBarCode());
                    item.setColorId(itemBo.getColorId());
                    item.setColorValue(itemBo.getColorValue());
                    item.setColorImage(itemBo.getColorImage());
                    item.setSizeId(itemBo.getSizeId());
                    item.setSizeValue(itemBo.getSizeValue());
                    item.setStyleId(itemBo.getStyleId());
                    item.setStyleValue(itemBo.getStyleValue());
                    item.setStandard(itemBo.getStandard());
                    item.setBrandNo(itemBo.getBrandNo());
                    item.setBrandName(itemBo.getBrandName());
                    item.setPrice(itemBo.getPrice());
                    item.setUpdateBy(username);
                    itemMapper.updateById(item);
                    existingMap.remove(itemBo.getId());
                } else {
                    item = new ErpSupplierProductItem();
                    item.setSupplierProductId(product.getId());
                    item.setSupplierId(product.getSupplierId());
                    item.setSkuCode(itemBo.getSkuCode());
                    item.setProductName(bo.getProductName());
                    item.setBarCode(itemBo.getBarCode());
                    item.setColorId(itemBo.getColorId());
                    item.setColorValue(itemBo.getColorValue());
                    item.setColorImage(itemBo.getColorImage());
                    item.setSizeId(itemBo.getSizeId());
                    item.setSizeValue(itemBo.getSizeValue());
                    item.setStyleId(itemBo.getStyleId());
                    item.setStyleValue(itemBo.getStyleValue());
                    item.setStandard(itemBo.getStandard());
                    item.setBrandNo(itemBo.getBrandNo());
                    item.setBrandName(itemBo.getBrandName());
                    item.setPrice(itemBo.getPrice());
                    item.setStatus(1);
                    item.setCreateBy(username);
                    itemMapper.insert(item);
                }
            }
        }

        for (ErpSupplierProductItem deletedItem : existingMap.values()) {
            itemMapper.deleteById(deletedItem.getId());
        }
        return ResultVo.success(product.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        LambdaQueryWrapper<ErpSupplierProductItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(ErpSupplierProductItem::getSupplierProductId, id);
        itemMapper.delete(itemWrapper);
        LambdaQueryWrapper<ErpSupplierGoodsPrice> priceWrapper = new LambdaQueryWrapper<>();
        priceWrapper.eq(ErpSupplierGoodsPrice::getSupplierProductId, id);
        supplierGoodsPriceMapper.delete(priceWrapper);
        this.removeById(id);
    }

    @Override
    public List<ErpSupplierProductItem> queryItemListByProductId(Long supplierProductId) {
        LambdaQueryWrapper<ErpSupplierProductItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ErpSupplierProductItem::getSupplierProductId, supplierProductId);
        return itemMapper.selectList(queryWrapper);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        ErpSupplierProduct product = this.getById(id);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        product.setStatus(status);
        this.updateById(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultVo linkGoodsFromLibrary(String username, SupplierGoodsLinkBo bo) {
        if (bo.getSupplierId() == null) return ResultVo.error(500, "供应商ID不能为空");
        if (bo.getGoodsId() == null) return ResultVo.error(500, "商品库SPU ID不能为空");
        if (bo.getSkus() == null || bo.getSkus().isEmpty()) return ResultVo.error(500, "请至少选择一个SKU");

        OGoods oGoods = goodsMapper.selectById(bo.getGoodsId());
        if (oGoods == null) return ResultVo.error(500, "商品库商品不存在");

        LambdaQueryWrapper<ErpSupplierProduct> productQuery = new LambdaQueryWrapper<>();
        productQuery.eq(ErpSupplierProduct::getSupplierId, bo.getSupplierId());
        productQuery.eq(ErpSupplierProduct::getErpGoodsId, bo.getGoodsId());
        ErpSupplierProduct product = this.getOne(productQuery);

        if (product == null) {
            product = new ErpSupplierProduct();
            product.setSupplierId(bo.getSupplierId());
            product.setProductName(oGoods.getName());
            product.setImageUrl(oGoods.getImage());
            product.setProductNum(oGoods.getGoodsNum());
            product.setCategoryId(oGoods.getCategoryId());
            product.setUnitName(oGoods.getUnitName());
            product.setLength(oGoods.getLength());
            product.setWidth(oGoods.getWidth());
            product.setHeight(oGoods.getHeight());
            product.setWeight(oGoods.getWeight());
            product.setErpGoodsId(bo.getGoodsId());
            product.setMerchantId(0L);
            product.setStatus(1);
            product.setCreateBy(username);
            product.setCreateTime(LocalDateTime.now());
            this.save(product);
        }

        final Long supplierProductId = product.getId();

        for (SupplierGoodsLinkBo.SkuItem skuItem : bo.getSkus()) {
            if (skuItem.getSkuId() == null) continue;

            OGoodsSku oGoodsSku = goodsSkuMapper.selectById(skuItem.getSkuId());
            if (oGoodsSku == null) continue;

            LambdaQueryWrapper<ErpSupplierProductItem> itemQuery = new LambdaQueryWrapper<>();
            itemQuery.eq(ErpSupplierProductItem::getSupplierProductId, supplierProductId);
            itemQuery.eq(ErpSupplierProductItem::getErpGoodsSkuId, skuItem.getSkuId());
            ErpSupplierProductItem existingItem = itemMapper.selectOne(itemQuery);
            Long itemId;

            if (existingItem != null) {
                if (skuItem.getPrice() != null) {
                    existingItem.setPrice(skuItem.getPrice());
                }
                existingItem.setUpdateBy(username);
                existingItem.setUpdateTime(LocalDateTime.now());
                itemMapper.updateById(existingItem);
                itemId = existingItem.getId();
            } else {
                ErpSupplierProductItem newItem = new ErpSupplierProductItem();
                newItem.setSupplierProductId(supplierProductId);
                newItem.setSupplierId(bo.getSupplierId());
                newItem.setSkuCode(oGoodsSku.getSkuCode());
                newItem.setProductName(oGoods.getName());
                newItem.setBarCode(oGoodsSku.getBarCode());
                newItem.setColorImage(oGoodsSku.getColorImage());
                newItem.setColorValue(oGoodsSku.getColorValue());
                newItem.setSizeValue(oGoodsSku.getSizeValue());
                newItem.setStyleValue(oGoodsSku.getStyleValue());
                newItem.setStandard(oGoodsSku.getSkuName());
                newItem.setPrice(skuItem.getPrice() != null ? skuItem.getPrice() : BigDecimal.ZERO);
                newItem.setErpGoodsId(bo.getGoodsId());
                newItem.setErpGoodsSkuId(skuItem.getSkuId());
                newItem.setStatus(1);
                newItem.setCreateBy(username);
                newItem.setCreateTime(LocalDateTime.now());
                itemMapper.insert(newItem);
                itemId = newItem.getId();
            }

            ErpSupplierGoodsPrice priceRecord = new ErpSupplierGoodsPrice();
            priceRecord.setSupplierId(bo.getSupplierId());
            priceRecord.setSupplierProductId(supplierProductId);
            priceRecord.setSupplierProductItemId(itemId);
            priceRecord.setSkuCode(oGoodsSku.getSkuCode());
            priceRecord.setPrice(skuItem.getPrice() != null ? skuItem.getPrice() : BigDecimal.ZERO);
            priceRecord.setMerchantId(0L);
            priceRecord.setStatus(1);
            priceRecord.setCreateBy(username);
            priceRecord.setCreateTime(LocalDateTime.now());
            supplierGoodsPriceMapper.insert(priceRecord);
        }

        return ResultVo.success();
    }
}
