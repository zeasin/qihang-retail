package cn.qihangerp.request;

import lombok.Data;

@Data
public class LogisticsCompanyRequest {
    private String name;
    private Integer status;
    private Integer platformId;
    private String type;
}

