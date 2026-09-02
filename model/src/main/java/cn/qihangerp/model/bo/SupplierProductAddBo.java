package cn.qihangerp.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商商品添加BO
 */
@Data
public class SupplierProductAddBo implements Serializable {
    private static final long serialVersionUID = 1L;

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
    private String remark;
    private List<SupplierProductItemBo> itemList;

    @Data
    public static class SupplierProductItemBo implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long id;
        private String skuCode;
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
    }
}
