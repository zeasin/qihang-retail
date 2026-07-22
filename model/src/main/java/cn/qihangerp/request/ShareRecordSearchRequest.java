package cn.qihangerp.request;

import lombok.Data;

@Data
public class ShareRecordSearchRequest {
    private Integer pageNum;
    private Integer pageSize;
    private String orderNum;
    private Integer shopType;
    private Long sharePartyId;
    private Integer status;
    private String startTime;
    private String endTime;
}
