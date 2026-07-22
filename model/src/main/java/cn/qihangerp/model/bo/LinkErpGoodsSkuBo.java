package cn.qihangerp.model.bo;

import lombok.Data;

@Data
public class LinkErpGoodsSkuBo {
    private String id;//主键ID（店铺商品SKU主键ID）
//    private String skuCode;//SKU编码
    private Long erpGoodsSkuId;//ERP商品库SkuId
}
