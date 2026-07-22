package cn.qihangerp.model.request;

import lombok.Data;

import java.util.List;

@Data
public class StockInCreateRequest {
    private Long warehouseId;//入库的仓库ID
    private String stockInNum;
    private Integer stockInType;
    private String sourceNo;
    private String stockInOperator;
    private String remark;
    private Long merchantId;
    private Long shopId;
    private List<StockInCreateItem> itemList;
}
