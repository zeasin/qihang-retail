package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class ShipStockingSearchRequest {
    private String orderNum;
    private String receiverName;
    private String receiverMobile;
    private Integer deliveryMethod;
    private Integer orderStatus;
    private String startTime;
    private String endTime;
}
