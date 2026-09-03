package cn.qihangerp.model.request;


import lombok.Data;

import java.util.List;

/**
 * 采购订单对象 scm_purchase_order
 * 
 * @author qihang
 * @date 2023-12-29
 */
@Data
public class PurchaseOrderStockInBo
{
    private static final long serialVersionUID = 1L;

    private Long id;//采购单id


    private String createBy;
    private String receiptTime;
    private String remark;


    private List<PurchaseOrderStockInItemBo> goodsList;



}
