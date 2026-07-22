package cn.qihangerp.model.request;

import lombok.Data;

import java.util.List;

/**
 * 本地仓入库
 */
@Data
public class StockInLocalRequest {
    private Long id;//入库ID
    private List<Item> items;

    @Data
    public static class Item {
        private Long itemId;//入库单itemId
        private Long skuId;//入库skuid
        private Integer inventoryMode;//仓库模式0普通1一物一码
        private Integer quantity;//入库数量
        private List<Batch> batchList;

        @Data
        public static class Batch{
            private String barcode;//条码
            private Float goldWeight;//金重
            private Float silverWeight;//银重
            private Float laborCost;//加工费
            private String certificateNo;//证书号
        }
    }

}
