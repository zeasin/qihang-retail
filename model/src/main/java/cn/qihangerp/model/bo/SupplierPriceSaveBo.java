package cn.qihangerp.model.bo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 保存供应商报价BO
 */
@Data
public class SupplierPriceSaveBo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long supplierId;
    private List<SkuPriceItem> skus;

    @Data
    public static class SkuPriceItem implements Serializable {
        private static final long serialVersionUID = 1L;

        private Long skuItemId;
        private Long erpSkuId;
        private BigDecimal price;
    }
}
