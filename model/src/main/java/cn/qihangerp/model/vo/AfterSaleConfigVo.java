package cn.qihangerp.model.vo;

import lombok.Data;

@Data
public class AfterSaleConfigVo {
    private Integer returnPeriodDays;
    private Integer exchangePeriodDays;
    private Double refundAuditThreshold;
    private String[] returnReasons;
}
