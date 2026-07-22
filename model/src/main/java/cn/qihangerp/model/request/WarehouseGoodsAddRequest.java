package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class WarehouseGoodsAddRequest {
    private Long warehouseId;//仓库id
    private Long erpGoodsSkuId;//商品库商品skuid
    private String skuId;//三方平台skuID
    private String unitName;//单位
    private String barCode;//条码
}
