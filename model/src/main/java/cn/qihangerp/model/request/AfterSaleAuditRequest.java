package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class AfterSaleAuditRequest {
    private Boolean approved;
    private String remark;
}
