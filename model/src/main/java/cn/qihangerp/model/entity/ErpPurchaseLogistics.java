package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 采购物流公司表
 * @TableName scm_logistics
 */
@TableName(value ="erp_purchase_logistics")
@Data
public class ErpPurchaseLogistics implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 物流公司编码
     */
    private String code;

    /**
     * 物流公司名称
     */
    private String name;

    /**
     * 备注
     */
    private String remark;

    /**
     * 状态（0禁用1启用）
     */
    private Integer status;
    private Long merchantId;
    private Long shopId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}