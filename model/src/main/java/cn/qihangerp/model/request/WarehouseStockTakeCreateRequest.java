package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class WarehouseStockTakeCreateRequest {
    private Long warehouseId;//盘点的仓库ID
    private String userName;
    private Long userId;
    private String remark;
    private Long merchantId;

}
