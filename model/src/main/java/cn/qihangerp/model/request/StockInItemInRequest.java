package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class StockInItemInRequest {
    private Long stockInId;
    private Long stockInItemId;
    private Integer intoQuantity;
    private Long warehouseId;
    private Long positionId;
//    private String positionNum;
    private String productionDate;
    private Integer period;
}
