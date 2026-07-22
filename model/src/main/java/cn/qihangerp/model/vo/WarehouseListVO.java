package cn.qihangerp.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 *
 */
@Data
public class WarehouseListVO implements Serializable {
    /**
     * 
     */
    @Schema(description = "仓库Id")
    private Long id;

    /**
     * 拥有者
     */
    @Schema(description = "拥有者")
    private String ownerNo;

    /**
     * 类型（1本地仓2云仓）
     */
    @Schema(description = "类型（1本地仓2云仓）")
    private Integer type;
    /**
     * 类型（LOCAL本地仓CLOUD系统云仓JDYC京东云仓Other其他）
     */
    @Schema(description = "仓库类型（LOCAL本地仓CLOUD系统云仓JDYC京东云仓）")
    private String warehouseType;
    private String loginName;

    /**
     * 云仓编码
     */
    @Schema(description = "编码")
    private String warehouseNo;

    /**
     * 名称
     */
    @Schema(description = "仓库名称")
    private String warehouseName;
    @Schema(description = "来源（0自有1总部分配）")
    private Integer warehouseSource;

    @Schema(description = "商家库房编号")
    private String erpWarehouseName;


//    /**
//     * type
//     */
//    private String type;

    /**
     * status
     */
    @Schema(description = "状态（0禁用1启用）")
    private String status;

    /**
     * 省
     */
    @Schema(description = "省")
    private String province;

    /**
     * 市
     */
    @Schema(description = "市")
    private String city;

    /**
     * 区
     */
    @Schema(description = "区")
    private String county;

    /**
     * 街道
     */
    @Schema(description = "街道")
    private String town;

    /**
     * 详细地址
     */
    @Schema(description = "详细地址")
    private String address;

    /**
     * 联系人
     */
    @Schema(description = "联系人")
    private String contacts;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建时间
     */
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新时间
     */
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    @Schema(description = "商户ID（0总部）")
    private Long merchantId;
    @Schema(description = "备注")
    private String remark;
    @Schema(description = "JDL接口类型")
    private Integer jdlApiType;
    private String merchantIds;
}