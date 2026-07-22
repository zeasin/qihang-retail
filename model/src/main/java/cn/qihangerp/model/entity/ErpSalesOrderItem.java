package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售单明细实体类
 * 对应表: erp_sales_order_item
 */
@Data
@TableName("erp_sales_order_item")
public class ErpSalesOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 商品名称
     */
    private String goodsName;

    /**
     * 商品编码
     */
    private String goodsNum;

    /**
     * 条码
     */
    private String barCode;

    /**
     * 规格信息
     */
    private String specInfo;

    /**
     * 单位
     */
    private String unit;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 单价
     */
    private BigDecimal price;

    /**
     * 小计
     */
    private BigDecimal subtotal;

    /**
     * 折扣金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal paidAmount;

    /**
     * 备注
     */
    private String remark;
}
