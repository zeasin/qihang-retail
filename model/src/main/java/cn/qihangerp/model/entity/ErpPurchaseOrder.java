package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 采购订单
 * @TableName scm_purchase_order
 */
@TableName(value ="erp_purchase_order")
@Data
public class ErpPurchaseOrder implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商id
     */
    private Long supplierId;

    /**
     * 订单编号
     */
    private String orderNum;

    /**
     * 订单日期
     */
    private String orderDate;

    /**
     * 订单创建时间
     */
    private Long orderTime;

    /**
     * 订单总金额
     */
    private BigDecimal orderAmount;

    /**
     * 物流费用
     */
    private BigDecimal shipAmount;

    /**
     * 备注
     */
    private String remark;

    /**
     * 订单状态 0待审核1已审核101供应商已确认102供应商已发货2已收货3已入库
     */
    private Integer status;

    /**
     * 采购单审核人
     */
    private String auditUser;

    /**
     * 审核时间
     */
    private Long auditTime;

    /**
     * 供应商确认时间
     */
    private LocalDateTime supplierConfirmTime;

    /**
     * 供应商发货时间
     */
    private LocalDateTime supplierDeliveryTime;

    /**
     * 收货时间
     */
    private LocalDateTime receivedTime;

    /**
     * 入库时间
     */
    private LocalDateTime stockInTime;

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
    private Long merchantId;
    private Long shopId;

    private Long warehouseId;
    private String warehouseName;
    private String warehouseType;

    @TableField(exist = false)
    private List<ErpPurchaseOrderItem> itemList;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}