package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class GoodsSearchRequest {
    private Long merchantId;
    private String name;
    private String number;
    private Long categoryId;
    private Integer status;
}
