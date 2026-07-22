package cn.qihangerp.model.vo;

import cn.qihangerp.utils.poi.Excel;
import lombok.Data;

@Data
public class GoodsInventoryListVo {
    /**
     * 商品规格id
     */
    @Excel(name = "SkuId",sort = 1)
    private String skuId;
    /**
     * 商品名
     */
    @Excel(name = "商品名",sort = 2)
    private String goodsName;

    /**
     * SKU名
     */
    @Excel(name = "Sku名",sort = 3)
    private String skuName;

    /**
     * 商品编码
     */
    @Excel(name = "商品编码",sort = 4)
    private String goodsNum;

    /**
     * 规格编码（唯一）
     */
    @Excel(name = "Sku编码",sort = 5)
    private String skuCode;

    /**
     * 当前库存
     */
    @Excel(name = "库存",sort = 6)
    private Integer quantity;

}
