package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 发货电子面单记录表（打单记录）
 * @TableName o_order_ship_waybill
 */
@Data
public class OOrderShipWaybill implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号
     */
    private String orderId;
    private Long shipOrderId;



    /**
     * 店铺id
     */
    private Long shopId;

    /**
     * 店铺类型
     */
    private Integer shopType;

    /**
     * 电子面单订单id(仅视频号)
     */
    private String waybillOrderId;

    /**
     * 快递单号
     */
    private String waybillCode;

    /**
     * 快递公司编码
     */
    private String logisticsCode;

    /**
     * 打印数据
     */
    private String printData;

    private String sign;
    private String params;

    /**
     * 状态（1已取号2已打印3已发货）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 更新人
     */
    private String updateBy;
    private String templateUrl;
    private String goodsDetail;
    private Long merchantId;
    @TableField(exist = false)
    private String printText;

    private static final long serialVersionUID = 1L;
}