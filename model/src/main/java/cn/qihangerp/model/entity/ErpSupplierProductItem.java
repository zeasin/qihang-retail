package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商商品表(SKU维度)
 * @TableName erp_supplier_product_item
 */
@TableName(value = "erp_supplier_product_item")
@Data
public class ErpSupplierProductItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long supplierProductId;
    private Long supplierId;
    private String skuCode;
    private String productName;
    private String barCode;
    private Long colorId;
    private String colorValue;
    private String colorImage;
    private Long sizeId;
    private String sizeValue;
    private Long styleId;
    private String styleValue;
    private String standard;
    private String brandNo;
    private String brandName;
    private BigDecimal price;
    private Long erpGoodsId;
    private Long erpGoodsSkuId;
    private Long warehouseGoodsId;
    private Integer status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private OGoodsSku erpGoodsSku;
}
