package cn.qihangerp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 *
 */
@Data
public class WarehouseMerchantQuery implements Serializable {



    /**
     * 状态（1启用0禁用）
     */
    @Schema(description = "状态（1启用0禁用）", example = "1")
    private String status;
    /**
     * 商户ID
     */
    private Long merchantId;
    /**
     * 店铺ID
     */
    private Long shopId;

}