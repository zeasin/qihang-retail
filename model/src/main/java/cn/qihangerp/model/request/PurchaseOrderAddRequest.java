package cn.qihangerp.model.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 采购订单对象 scm_purchase_order
 * 
 * @author qihang
 * @date 2023-12-29
 */
@Data
public class PurchaseOrderAddRequest
{
    private static final long serialVersionUID = 1L;


    /** 供应商id */
    private Long contactId;

    /** 订单编号 */
    private String orderNo;

    /** 订单日期 */
    private String orderDate;


    /** 订单总金额 */
    private BigDecimal orderAmount;
    private String createBy;

    private Long merchantId;
    private Long shopId;
    // 状态（订单状态 0待审核1已审核101供应商已确认102供应商已发货2已收货3已入库）
    private Integer status;

    private List<PurchaseOrderAddItemBo> goodsList;


}
