package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.model.entity.OGoodsInventory;
import cn.qihangerp.model.entity.OGoodsInventoryBatch;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OGoodsInventoryBatchService;
import cn.qihangerp.service.OGoodsInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/inventory")
public class PosInventoryController extends BaseController {

    private final OGoodsInventoryService goodsInventoryService;
    private final OGoodsInventoryBatchService inventoryBatchService;

    @GetMapping("/sku/{skuId}")
    public AjaxResult getInventoryBySkuId(@PathVariable("skuId") String skuId) {
        LambdaQueryWrapper<OGoodsInventory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OGoodsInventory::getSkuId, skuId);
        OGoodsInventory inventory = goodsInventoryService.getOne(queryWrapper);
        return success(inventory);
    }

    @GetMapping("/sku/{skuId}/batches")
    public AjaxResult getBatchesBySkuId(@PathVariable("skuId") String skuId) {
        LambdaQueryWrapper<OGoodsInventoryBatch> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OGoodsInventoryBatch::getSkuId, skuId);
        queryWrapper.gt(OGoodsInventoryBatch::getCurrentQty, 0);
        List<OGoodsInventoryBatch> batches = inventoryBatchService.list(queryWrapper);
        return success(batches);
    }
}