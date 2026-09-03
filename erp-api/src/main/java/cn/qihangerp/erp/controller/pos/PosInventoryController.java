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

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/pos-api/inventory")
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

    /**
     * 批量查询SKU库存（用于商品列表预加载）
     */
    @PostMapping("/batch")
    public AjaxResult batchInventory(@RequestBody List<String> skuIds) {
        if (skuIds == null || skuIds.isEmpty()) return success(Collections.emptyMap());

        List<Long> longIds = skuIds.stream().map(Long::parseLong).toList();

        List<OGoodsInventory> invList = goodsInventoryService.list(
            new LambdaQueryWrapper<OGoodsInventory>()
                .in(OGoodsInventory::getSkuId, longIds));

        Map<Long, OGoodsInventory> invMap = invList.stream()
            .collect(Collectors.toMap(OGoodsInventory::getSkuId, v -> v, (a, b) -> a));

        List<OGoodsInventoryBatch> batchList = inventoryBatchService.list(
            new LambdaQueryWrapper<OGoodsInventoryBatch>()
                .in(OGoodsInventoryBatch::getSkuId, longIds)
                .gt(OGoodsInventoryBatch::getCurrentQty, 0));

        Map<Long, List<OGoodsInventoryBatch>> batchMap = batchList.stream()
            .collect(Collectors.groupingBy(OGoodsInventoryBatch::getSkuId));

        Map<String, Object> result = new HashMap<>();
        Map<String, OGoodsInventory> invResult = new HashMap<>();
        invMap.forEach((k, v) -> invResult.put(String.valueOf(k), v));
        Map<String, List<OGoodsInventoryBatch>> batchResult = new HashMap<>();
        batchMap.forEach((k, v) -> batchResult.put(String.valueOf(k), v));
        result.put("inventory", invResult);
        result.put("batches", batchResult);
        return success(result);
    }
}
