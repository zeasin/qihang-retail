package cn.qihangerp.model.request;

import lombok.Data;

@Data
public class WarehouseShareMerchantRequest {
    private Long id;//仓库Id
    private Long[] merchantIds;//商户ID
}
