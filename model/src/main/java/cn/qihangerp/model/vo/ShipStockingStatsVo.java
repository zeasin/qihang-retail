package cn.qihangerp.model.vo;

import lombok.Data;

@Data
public class ShipStockingStatsVo {
    private Long pendingCount;
    private Long todayShipped;
    private Long riderCount;
    private Long merchantCount;
    private Long pickupCount;
}
