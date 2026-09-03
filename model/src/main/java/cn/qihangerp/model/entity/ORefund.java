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

    /** 类型(1-售前退款 10-退货 11-仅退款) */
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

    private String remark;

    /** 状态（10001待审核 10010退款完成 10011退款关闭 10090退款中） */
    private Integer status;

    private LocalDateTime createTime;

    private String createBy;

    private LocalDateTime updateTime;

    private String updateBy;

    private Long merchantId;

    /** ERP状态 0待处理 10已退款 */
    private Integer erpStatus;
}
