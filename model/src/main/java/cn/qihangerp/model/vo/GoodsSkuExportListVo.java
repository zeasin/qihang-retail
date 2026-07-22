package cn.qihangerp.model.vo;

import cn.qihangerp.utils.poi.Excel;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 *
 */

@Data
public class GoodsSkuExportListVo implements Serializable {
    /**
     * 主键id
     */
    @Excel(name = "SkuId",sort = 1)
    @TableId(type = IdType.AUTO)
    private String id;

    /**
     * 外键（o_goods）
     */
    @Excel(name = "商品Id",sort = 2)
    private String goodsId;

    /**
     * 外部erp系统商品id
     */
    private String outerErpGoodsId;

    /**
     * 外部erp系统skuId(唯一)
     */
    @Excel(name = "外部SkuId",sort = 3)
    private String outerErpSkuId;

    /**
     * 商品名
     */
    @Excel(name = "商品名",sort = 4)
    private String goodsName;

    /**
     * 商品编码
     */
    @Excel(name = "商品编码",sort = 5)
    private String goodsNum;

    @Excel(name = "单位",sort = 6)
    private String unit;

    /**
     * 规格名
     */
    @Excel(name = "规格",sort = 7)
    private String skuName;

    /**
     * 规格编码
     */
    @Excel(name = "Sku编码",sort = 8)
    private String skuCode;



    /**
     * 库存条形码
     */
    @Excel(name = "条形码",sort = 9)
    private String barCode;

    /**
     * 预计采购价格
     */
    @Excel(name = "采购价",sort = 10)
    private BigDecimal purPrice;

    /**
     * 建议零售价
     */
    @Excel(name = "零售价",sort = 11)
    private BigDecimal retailPrice;



    /**
     * 状态
     */
    private Integer status;



    /**
     * 商户ID
     */
    private Long merchantId;

    private static final long serialVersionUID = 1L;
}