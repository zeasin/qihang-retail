package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class ManualShipSearchRequest {
    private String orderNum;
    private String receiverName;
    private String receiverMobile;
    private Integer shipStatus;
    private String startTime;
    private String endTime;
}
