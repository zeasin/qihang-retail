package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.OGoodsInventoryMapper;
import cn.qihangerp.model.entity.ErpWarehouse;
import cn.qihangerp.model.entity.OGoodsInventory;
import cn.qihangerp.service.ErpWarehouseService;
import cn.qihangerp.service.OGoodsInventoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class OGoodsInventoryServiceImpl
        extends ServiceImpl<OGoodsInventoryMapper, OGoodsInventory>
        implements OGoodsInventoryService {

    private final ErpWarehouseService warehouseService;

    @Override
    public PageResult<OGoodsInventory> queryPageList(OGoodsInventory bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OGoodsInventory> qw = new LambdaQueryWrapper<OGoodsInventory>()
                .eq(bo.getWarehouseId() != null, OGoodsInventory::getWarehouseId, bo.getWarehouseId())
                .eq(bo.getMerchantId() != null, OGoodsInventory::getMerchantId, bo.getMerchantId())
                .eq(bo.getSkuId() != null, OGoodsInventory::getSkuId, bo.getSkuId())
                .eq(bo.getGoodsId() != null, OGoodsInventory::getGoodsId, bo.getGoodsId())
                .eq(StringUtils.hasText(bo.getGoodsNum()), OGoodsInventory::getGoodsNum, bo.getGoodsNum())
                .eq(StringUtils.hasText(bo.getSkuCode()), OGoodsInventory::getSkuCode, bo.getSkuCode())
                .like(StringUtils.hasText(bo.getGoodsName()), OGoodsInventory::getGoodsName, bo.getGoodsName())
                .eq(OGoodsInventory::getIsDelete, 0);
        IPage<OGoodsInventory> page = this.baseMapper.selectPage(pageQuery.build(), qw);

        // 填充仓库名称
        List<Long> warehouseIds = page.getRecords().stream()
                .map(OGoodsInventory::getWarehouseId)
                .filter(id -> id != null)
                .distinct()
                .toList();
        if (!warehouseIds.isEmpty()) {
            Map<Long, ErpWarehouse> whMap = warehouseService.listByIds(warehouseIds).stream()
                    .collect(Collectors.toMap(ErpWarehouse::getId, w -> w));
            page.getRecords().forEach(inv -> {
                ErpWarehouse wh = whMap.get(inv.getWarehouseId());
                if (wh != null) {
                    inv.setWarehouseName(wh.getWarehouseName());
                    inv.setWarehouseType(wh.getWarehouseType());
                }
            });
        }

        return PageResult.build(page);
    }

    @Override
    public boolean deductStock(Long inventoryId, Integer quantity) {
        OGoodsInventory inv = this.getById(inventoryId);
        if (inv == null || inv.getQuantity() < quantity) return false;

        OGoodsInventory update = new OGoodsInventory();
        update.setId(inventoryId);
        update.setQuantity(inv.getQuantity() - quantity);
        update.setAvailableQuantity(inv.getAvailableQuantity() - quantity);
        return this.updateById(update);
    }

    @Override
    public boolean addStock(Long inventoryId, Integer quantity) {
        OGoodsInventory inv = this.getById(inventoryId);
        if (inv == null) return false;

        OGoodsInventory update = new OGoodsInventory();
        update.setId(inventoryId);
        update.setQuantity(inv.getQuantity() + quantity);
        update.setAvailableQuantity(inv.getAvailableQuantity() + quantity);
        return this.updateById(update);
    }

    @Override
    public boolean lockStock(Long inventoryId, Integer quantity) {
        OGoodsInventory inv = this.getById(inventoryId);
        if (inv == null) return false;
        if (quantity < 0 && inv.getLockedQuantity() < -quantity) return false;

        OGoodsInventory update = new OGoodsInventory();
        update.setId(inventoryId);
        update.setLockedQuantity(inv.getLockedQuantity() + quantity);
        update.setAvailableQuantity(inv.getAvailableQuantity() - quantity);
        return this.updateById(update);
    }
}
