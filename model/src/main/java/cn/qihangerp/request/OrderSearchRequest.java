package cn.qihangerp.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderSearchRequest {
    private Long shopId;
    private Integer shopType;
//    private Integer platformId;
    private String orderNum;
    /**
     * 订单来源：POS=POS收银、MT_FLASH=美团闪购、MT_WM=美团外卖、JD_DJ=京东到家等
     */
    private String orderSource;
    private Integer orderStatus;
    private Integer refundStatus;
    private String startTime;
    private String endTime;
    private Integer shipType;
    private Integer shipStatus;
    private Integer distStatus;
    private String receiver;
    private String receiverName;
    private String receiverMobile;
    private String shippingNumber;
    private Long merchantId;
    /**
     * 销售员ID
     */
    private String salesmanId;
    private List<Long> orderIds;
}
