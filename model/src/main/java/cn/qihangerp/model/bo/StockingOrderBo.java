package cn.qihangerp.model.bo;

import lombok.Data;

@Data
public class StockingOrderBo {

    private String orderNum;
    private String startTime;
    private String endTime;
    private Integer stockingStatus;//状态0待备货1备货中2备货完成
    private Long merchantId;
}
