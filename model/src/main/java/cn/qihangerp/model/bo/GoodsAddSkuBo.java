package cn.qihangerp.model.bo;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class GoodsAddSkuBo {
    private Long colorId;
    private String colorValue;
    private Long sizeId;
    private String sizeValue;
    private Long styleId;
    private String styleValue;
    private String skuName;
    private String specNum;
    private String barCode;//产品条形码

    private BigDecimal purPrice;
    private BigDecimal retailPrice;

    //外部ERP商品Sku Id
    private String outerErpSkuId;
    private String unit;

    private String volume;//	体积
    private Integer length;//	长度（mm)
    private Integer width;//	长度（mm)
    private Integer height;//	长度（mm)
    private Double weight;//	重量（g)
    private String image;//	sku图片地址
    /** 金重量（g) */
    private Double weight1;

    /** 银重量（g) */
    private Double weight2;

    /** 工时 */
    private Double weight3;
}
