package cn.qihangerp.common.bo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ShipOrderAllocateShipBo {
    private String Id;//订单id

    private BigDecimal purAmount;//采购金额
    private String receiverName;//
    private String receiverMobile;//
    private String address;//
    private String remark;
    private String buyerMemo;
    private String sellerMemo;
}
