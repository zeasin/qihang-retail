package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class ShipDeliveryRequest {
    private String orderId;
    private Integer deliveryMethod;
    private String carrierName;
    private String trackingNumber;
    private String remark;
}
