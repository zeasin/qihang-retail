package cn.qihangerp.request;

import lombok.Data;

@Data
public class ShareRuleSearchRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String ruleName;
    private Long goodsId;
    private Long categoryId;
    private Integer shopType;
    private Long sharePartyId;
    private Integer status;
}
