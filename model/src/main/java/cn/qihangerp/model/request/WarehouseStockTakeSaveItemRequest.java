package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class WarehouseStockTakeSaveItemRequest {
    private Long id;//盘点ID
    private Long goodsId;//仓库商品ID

}
