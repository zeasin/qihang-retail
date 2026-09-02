package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 供应商商品表(SPU维度)
 * @TableName erp_supplier_product
 */
@TableName(value = "erp_supplier_product")
@Data
public class ErpSupplierProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long supplierId;
    private String productName;
    private String imageUrl;
    private String productNum;
    private Long categoryId;
    private Long brandId;
    private String unitName;
    private Double length;
    private Double width;
    private Double height;
    private Double weight;
    private Integer status;
    private Long erpGoodsId;
    private Long merchantId;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private Integer skuCount;
    @TableField(exist = false)
    private String supplierName;
    @TableField(exist = false)
    private List<ErpSupplierProductItem> skuList;
}
