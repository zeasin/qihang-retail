package cn.qihangerp.model.vo;

import lombok.Data;

@Data
public class AfterSaleStatsVo {
    private Long todayCount;
    private Double todayRefundAmount;
    private Long pendingAudit;
    private Long pendingProcess;
    private Long totalCompleted;
}
