package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class RiderDeliverySearchRequest {
    private String orderNum;
    private String receiverName;
    private String receiverMobile;
    private Integer waybillStatus;
    private String startTime;
    private String endTime;
}
