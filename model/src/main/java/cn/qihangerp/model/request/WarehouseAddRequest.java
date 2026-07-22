package cn.qihangerp.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 *
 */
@Data
public class WarehouseAddRequest implements Serializable {

    /**
     * 类型（LOCAL本地仓CLOUD系统云仓JDYC京东云仓Other其他）
     */
    @Schema(description = "仓库类型（LOCAL本地仓CLOUD系统云仓JDYC京东云仓）", example = "LOCAL")
    private String warehouseType;
    /**
     * 云仓编码
     */
    @Schema(description = "仓库编码", example = "")
    private String warehouseNo;

    /**
     * 云仓名称
     */
    @Schema(description = "云仓名称", example = "")
    private String warehouseName;
    /**
     * status
     */
    @Schema(description = "状态（1启用0禁用）", example = "1")
    private String status;

    @Schema(description = "省", example = "")
    private String province;
    @Schema(description = "市", example = "")
    private String city;
    @Schema(description = "区", example = "")
    private String county;
    @Schema(description = "详细地址", example = "")
    private String address;
    @Schema(description = "备注", example = "")
    private String remark;

}