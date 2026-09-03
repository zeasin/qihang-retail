package cn.qihangerp.model.request;

import lombok.Data;

/**
 * 本地仓单条入库请求
 */
@Data
public class StockInLocalItemRequest {
    private Long entryItemId;// 入库单明细ID
    private Long entryId;// 入库单ID
    private Long skuId;// SKU ID
    private Integer quantity;// 入库数量
}
