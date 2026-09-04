package cn.qihangerp.model.vo;

import cn.qihangerp.model.entity.OGoodsInventory;
import cn.qihangerp.model.entity.OGoodsInventoryBatch;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PosBatchInventoryVo {
    private Map<String, OGoodsInventory> inventory;
    private Map<String, List<OGoodsInventoryBatch>> batches;
}
