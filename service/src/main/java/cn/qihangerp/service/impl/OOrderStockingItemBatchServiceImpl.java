package cn.qihangerp.service.impl;

import cn.qihangerp.model.entity.OOrderStockingItemBatch;
import cn.qihangerp.mapper.OOrderStockingItemBatchMapper;
import cn.qihangerp.service.OOrderStockingItemBatchService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 发货备货批次表 服务实现
 */
@Service
public class OOrderStockingItemBatchServiceImpl extends ServiceImpl<OOrderStockingItemBatchMapper, OOrderStockingItemBatch> implements OOrderStockingItemBatchService {

    /**
     * 根据发货备货明细ID查询批次列表
     * @param orderStockingItemId 发货备货明细ID
     * @return 批次列表
     */
    @Override
    public List<OOrderStockingItemBatch> getBatchListByOrderStockingItemId(Long orderStockingItemId) {
        return baseMapper.selectList(new LambdaQueryWrapper<OOrderStockingItemBatch>()
                .eq(OOrderStockingItemBatch::getOrderStockingItemId, orderStockingItemId));
    }

    /**
     * 根据发货备货ID查询批次列表
     * @param orderStockingId 发货备货ID
     * @return 批次列表
     */
    @Override
    public List<OOrderStockingItemBatch> getBatchListByOrderStockingId(Long orderStockingId) {
        return baseMapper.selectList(new LambdaQueryWrapper<OOrderStockingItemBatch>()
                .eq(OOrderStockingItemBatch::getOrderStockingId, orderStockingId));
    }
}
