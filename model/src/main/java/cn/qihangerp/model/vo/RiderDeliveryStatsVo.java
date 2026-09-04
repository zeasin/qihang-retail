package cn.qihangerp.model.vo;

import lombok.Data;

@Data
public class RiderDeliveryStatsVo {
    private Long pendingCount;
    private Long todayPrinted;
    private Long todayShipped;
    private Long totalPending;
}
