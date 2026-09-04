package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class AfterSaleSearchRequest {
    private String refundNum;
    private String orderNum;
    private Integer refundType;
    private Integer erpStatus;
    private Integer shopId;
    private String goodsName;
    private String startTime;
    private String endTime;
}
