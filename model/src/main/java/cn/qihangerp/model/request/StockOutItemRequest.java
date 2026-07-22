package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class StockOutItemRequest {
    private Long entryItemId;
    private Long entryId;
    private String skuId;
    private Long positionId;
    private Long batchId;
    private Integer outQty;
    private Integer originalQuantity;
    private Integer outQuantity;

}
