package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 云仓承运商
 * @TableName erp_cloud_warehouse_shipper
 */
@TableName(value ="erp_warehouse_shipper")
@Data
public class ErpWarehouseShipper implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户ID
     */
    private Long merchantId;
    private Long warehouseId;

    /**
     * CLPS事业部编号
     */
    private String ownerNo;

    /**
     * CLPS承运商编号
     */
    private String shipperNo;

    /**
     * 承运商名称
     */
    private String shipperName;

    /**
     * 承运商类型：1-京东配送；2-京配转三方；3-转采三方；4-自采三方；5-3PL
     */
    private String type;

    /**
     * 承运商状态：0-停用，1-启用
     */
    private String status;

    /**
     * 是否支持货到付款：1 -否， 2- 是
     */
    private String isCod;

    /**
     * 是否使用标准包裹签模板：1-否，2-是
     */
    private String isTemplate;

    /**
     * 联系人
     */
    private String contacts;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}