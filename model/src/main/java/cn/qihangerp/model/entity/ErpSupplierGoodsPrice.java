package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商商品报价表
 * @TableName erp_supplier_goods_price
 */
@TableName(value = "erp_supplier_goods_price")
@Data
public class ErpSupplierGoodsPrice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long supplierId;
    private Long supplierProductId;
    private Long supplierProductItemId;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private LocalDateTime validStartTime;
    private LocalDateTime validEndTime;
    private Long merchantId;
    private Integer status;
    private String remark;
    private String createBy;
    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
