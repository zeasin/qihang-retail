package cn.qihangerp.model.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsSaleReport {
    private String skuCode;
    private String goodsName;
    private String skuName;
    /**
     * 订单总数
     */
    private Integer orderTotal;

    /**
     * 订单总金额（当前货币）
     */
    private BigDecimal orderAmount;

    /**
     * 刷单数量
     */
    private Integer falseOrderTotal;

    /**
     * 刷单金额（当前货币）
     */
    private BigDecimal falseOrderAmount;

    /**
     * 刷单金额（人民币，包含服务费）
     */
    private BigDecimal falseOrderAmount1;

    /**
     * 真实订单数
     */
    private Integer trueOrderTotal;

    /**
     * 真实订单金额（当前货币）
     */
    private BigDecimal trueOrderAmount;

    /**
     * 广告支出
     */
    private BigDecimal adFee;

    /**
     * 广告点击
     */
    private Integer adClick;

    private Long shopId;
    private Double exchangeRate;
}
