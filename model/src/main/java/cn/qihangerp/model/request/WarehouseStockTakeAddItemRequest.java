package cn.qihangerp.model.request;

import lombok.Data;

import java.util.List;

@Data
public class WarehouseStockTakeAddItemRequest {
    private Long id;//ID
    private List<Item> itemList;

    @Data
    public static class Item{
        private Long id;//商品主键id
        private Integer quantity;//盘点结果
    }
}
