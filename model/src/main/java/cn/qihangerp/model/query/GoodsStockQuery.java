package cn.qihangerp.model.query;

import lombok.Data;

@Data
public class GoodsStockQuery {
    /**
     * 商户id
     */
    private Long merchantId;
    /**
     * 店铺ID：0-表示非店铺自己的
     */
    private Long shopId;
    /**
     * 仓库id
     */
    private Long warehouseId;

    /**
     * 商品id
     */
    private String goodsId;

    /**
     * 商品规格id
     */
    private String skuId;

    /**
     * 商品编码
     */
    private String goodsNum;

    /**
     * 规格编码（唯一）
     */
    private String skuCode;

    /**
     * 商品名
     */
    private String goodsName;
}
