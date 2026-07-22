package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 销售单实体类
 * 对应表: erp_sales_order
 */
@Data
@TableName("erp_sales_order")
public class ErpSalesOrder {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 会员ID
     */
    private Long memberId;

    /**
     * 会员手机号
     */
    private String memberPhone;

    /**
     * 会员姓名
     */
    private String memberName;

    /**
     * 销售员ID
     */
    private Long salesmanId;

    /**
     * 销售员姓名
     */
    private String salesmanName;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal paidAmount;

    /**
     * 支付方式
     */
    private String payMethod;

    /**
     * 订单状态：0待审核/1已审核/2已出库/3已完成/4已取消
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 审核人
     */
    private String auditor;

    /**
     * 订单明细（非数据库字段）
     */
    @TableField(exist = false)
    private List<ErpSalesOrderItem> items;
}
