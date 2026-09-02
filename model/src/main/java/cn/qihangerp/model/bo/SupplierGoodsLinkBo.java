package cn.qihangerp.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商商品库关联BO
 */
@Data
public class SupplierGoodsLinkBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long supplierId;
    private Long goodsId;
    private List<SkuItem> skus;

    @Data
    public static class SkuItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long skuId;
        private BigDecimal price;
        private String skuCode;
        private String skuName;
    }
}
