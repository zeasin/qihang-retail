package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class AfterSaleApplyRequest {
    private Long orderId;
    private Long orderItemId;
    private Integer quantity;
    private Double refundFee;
    private Integer refundType;
    private String refundReason;
    private String remark;
    private Long exchangeGoodsSkuId;
    private Integer exchangeQuantity;
}
