package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 售后退款表
 * 对应表: o_refund
 */
@Data
@TableName("o_refund")
public class ORefund {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String refundNum;

    /** 类型(1-售前退款 10-退货退款 11-仅退款 20-换货) */
    private Integer refundType;

    private Long shopId;

    private Integer shopType;

    private Double orderAmount;

    private Float refundFee;

    private String refundReason;

    private String orderNum;

    private String orderItemNum;

    private String skuId;

    private Long goodsId;

    private Long goodsSkuId;

    private String skuNum;

    /** 买家是否需要退货 1是 0否 */
    private Integer hasGoodReturn;

    private String goodsName;

    private String goodsSku;

    private String goodsImage;

    private Long quantity;

    /** 退货物流公司 */
    private String returnLogisticsCompany;

    /** 退货物流单号 */
    private String returnLogisticsCode;

    /** 收货时间（退货收货） */
    private LocalDateTime receiveTime;

    private String remark;

    /** 状态（10001待审核 10010退款完成 10011退款关闭 10090退款中） */
    private Integer status;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;

    private Integer erpPushStatus;

    private String erpPushResult;

    private LocalDateTime erpPushTime;

    /** 是否处理0未处理1已处理 */
    private Integer hasProcessing;

    /** 处理id */
    private Long afterSaleId;

    private Long merchantId;

    private String platformStatus;

    private String platformStatusText;

    /**
     * ERP售后状态（简化码）
     * 0待审核 1待退货 2待退款 3待换发 10已完成 11已取消 12已拒绝
     */
    private Integer erpStatus;

    /** 发货物流公司（换货发货） */
    private String sendLogisticsCompany;

    /** 发货物流单号（换货发货） */
    private String sendLogisticsCode;

    /** 处理类型 */
    private String processType;

    /** 订单发货状态 0未发货 1已发货 */
    private Integer shippingStatus;

    /** 换货商品名称 */
    private String exchangeGoodsName;

    /** 换货商品价格(单位分) */
    private Integer exchangeGoodsPrice;

    /** 申请换货的数量 */
    private Integer exchangeGoodsNum;

    /** 换货商品规格ID */
    private String exchangeSkuId;

    /** 换货商品库SkuId */
    private Long exchangeErpGoodsSkuId;

    /** 换货ERP订单Id */
    private Long exchangeErpOrderId;

    /** 审核人 */
    private String auditBy;

    /** 审核时间 */
    private LocalDateTime auditTime;

    /** 审核备注 */
    private String auditRemark;

    /** 退款方式：cash现金/original原路退回/balance退到余额 */
    private String refundMethod;

    /** 退款执行时间 */
    private LocalDateTime refundTime;

    /** 退款执行人 */
    private String refundBy;

    /** 退货收货人 */
    private String receiveBy;
}
