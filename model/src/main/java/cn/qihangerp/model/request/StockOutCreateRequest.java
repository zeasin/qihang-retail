package cn.qihangerp.model.request;

import cn.qihangerp.model.vo.GoodsSkuInventoryVo;
import lombok.Data;

import java.util.List;

@Data
public class StockOutCreateRequest {
    private Long shopId;
    private Long shopGroupId;
    private Long warehouseId;
    private String outNum;
    private Integer type;
    private String sourceNo;
    private String operator;
    private String remark;
    private Long merchantId;
    private Long vendorId;
    private List<GoodsSkuInventoryVo> itemList;
}
