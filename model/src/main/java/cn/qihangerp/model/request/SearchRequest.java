package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class SearchRequest {
    // 供应商id
    private Integer supplierId;
    private String orderNum;
    private String orderStatus;
    private String startTime;
    private String endTime;
    private Long merchantId;
    private Long shopId;
    private String orderDate;//下单日期
    private String shipTime;//发货日期

}
