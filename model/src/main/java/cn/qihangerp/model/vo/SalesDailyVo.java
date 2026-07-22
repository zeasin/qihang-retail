package cn.qihangerp.model.vo;

import lombok.Data;

@Data
public class SalesDailyVo {
    private String date;
    private Integer count;
    private Integer waitSend;
    private Double amount;
}
