package cn.qihangerp.request;

import lombok.Data;

@Data
public class SupplierShipOrderSearchRequest {

    private String orderNum;
    private Integer sendStatus;
    private Integer orderStatus;
    private String startTime;
    private String endTime;
    private Long supplierId;
    private Long warehouseId;
//    private Integer platformId;
    private Integer waybillStatus;
    private Integer stockingStatus;//状态0待备货1备货中2备货完成
    private Long shopId;
    /**
     * 店铺类型
     */
    private Integer shopType;
    private Long merchantId;
    /**
     * 类型：0本地仓库备货  300供应商发货 100京东云仓发货 110吉客云云仓发货 200系统云仓发货
     * EnumShipType
     */
    private Integer type;

}
