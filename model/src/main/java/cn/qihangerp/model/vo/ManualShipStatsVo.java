package cn.qihangerp.model.vo;

import lombok.Data;

@Data
public class ManualShipStatsVo {
    private Long pendingCount;
    private Long todayConfirmed;
    private Long todayTotal;
    private Long totalPending;
}
