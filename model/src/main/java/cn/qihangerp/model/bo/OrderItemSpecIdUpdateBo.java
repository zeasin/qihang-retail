package cn.qihangerp.model.bo;

import lombok.Data;

@Data
public class OrderItemSpecIdUpdateBo {
    private Long orderItemId;
    private Long erpGoodsSpecId;
}
