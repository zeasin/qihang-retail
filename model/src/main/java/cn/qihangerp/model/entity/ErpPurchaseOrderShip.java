package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单物流表
 * @TableName scm_purchase_order_ship
 */
@TableName(value ="erp_purchase_order_ship")
@Data
public class ErpPurchaseOrderShip implements Serializable {
    /**
     * 采购单ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 供应商id
     */
    private Long supplierId;

    /**
     * 
     */
    private Long orderId;

    /**
     * 物流公司
     */
    private String shipCompany;

    /**
     * 物流单号
     */
    private String shipNum;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiptTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 状态（0未收货1已收货2已入库）
     */
    private Integer status;

    /**
     * 说明
     */
    private String remark;

    /**
     * 退回数量
     */
    private Integer backCount;

    /**
     * 入库时间
     */
    private LocalDateTime stockInTime;

    /**
     * 入库数量
     */
    private Integer stockInCount;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 采购订单日期
     */
    private String orderDate;

    /**
     * 采购订单编号
     */
    private String orderNum;

    /**
     * 采购订单商品规格数
     */
    private Integer orderSpecUnit;

    /**
     * 采购订单商品数
     */
    private Integer orderGoodsUnit;

    /**
     * 采购订单总件数
     */
    private Integer orderSpecUnitTotal;
    private Long merchantId;
    private Long shopId;

    private Long warehouseId;
    private String warehouseName;
    private String warehouseType;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}