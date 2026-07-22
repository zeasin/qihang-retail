package cn.qihangerp.model.vo;

import lombok.Data;

import java.util.List;

/**
 * 订单推送京东云仓vo
 */
@Data
public class OrderPushJdlVo {
    private Long orderId;
    private Long merchantId;
    private String orderNo;//订单号
    private String receiverName;
    private String receiverMobile;
    private String receiverProvince;
    private String receiverCity;
    private String receiverDistrict;
    private String receiverTown;
    private String receiverAddress;
    private String buyerMessage;
    private String sellerMessage;
    private String remark;
    private List<OrderItem> items;
    @Data
    public static class OrderItem{
        private String jdlItemId;//京东云仓商品编码
//        private String skuCode;//商家商品编码
        private String goodsName;
        private Integer qty;
        private String remark;
    }
}
