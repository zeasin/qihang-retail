package cn.qihangerp.model.bo;

import lombok.Data;

import java.io.Serializable;

/**
 * 订单明细表
 * @TableName o_order_item
 */
@Data
public class StockingOrderItemBo implements Serializable {

    //状态0待备货1备货中2备货完成
    private Integer stockingStatus;

    private String orderNum;
    private String goodsNum;
    /**
     * 商品规格编码
     */
    private String skuCode;
    private Long merchantId;

    private static final long serialVersionUID = 1L;


}