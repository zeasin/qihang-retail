package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发货备货批次表
 */
@Data
@TableName("o_order_stocking_item_batch")
public class OOrderStockingItemBatch implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 发货备货明细ID
     */
    private Long orderStockingItemId;

    /**
     * 发货备货ID
     */
    private Long orderStockingId;

    /**
     * 商户ID
     */
    private Long merchantId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 订单ID
     */
    private String orderId;

    /**
     * 订单商品ID
     */
    private String orderItemId;

    /**
     * 批次ID
     */
    private Long batchId;

    /**
     * 批次号
     */
    private String batchNum;

    /**
     * 库存ID
     */
    private Long inventoryId;

    /**
     * 商品ID
     */
    private Long goodsId;

    /**
     * 商品编码
     */
    private String goodsNo;

    /**
     * 出库数量
     */
    private Integer quantity;

    /**
     * 单位成本
     */
    private BigDecimal unitCost;

    /**
     * 总成本
     */
    private BigDecimal totalCost;

    /**
     * 仓库ID
     */
    private Long warehouseId;

    /**
     * 仓库类型
     */
    private String warehouseType;

    /**
     * 库存模式：0-传统SKU模式 1-一物一码模式
     */
    private Integer inventoryMode;

    /**
     * 条码
     */
    private String barcode;

    /**
     * 实际金重
     */
    private BigDecimal actualGoldWeight;

    /**
     * 实际银重
     */
    private BigDecimal actualSilverWeight;

    /**
     * 工费
     */
    private BigDecimal laborCost;

    /**
     * 鉴定证书号
     */
    private String certificateNo;

    /**
     * 采购价格
     */
    private BigDecimal purPrice;

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
}