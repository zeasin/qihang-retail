package cn.qihangerp.service;

import cn.qihangerp.model.entity.OOrderStockingItemBatch;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 发货备货批次表 服务
 */
public interface OOrderStockingItemBatchService extends IService<OOrderStockingItemBatch> {
    
    /**
     * 根据发货备货明细ID查询批次列表
     * @param orderStockingItemId 发货备货明细ID
     * @return 批次列表
     */
    List<OOrderStockingItemBatch> getBatchListByOrderStockingItemId(Long orderStockingItemId);
    
    /**
     * 根据发货备货ID查询批次列表
     * @param orderStockingId 发货备货ID
     * @return 批次列表
     */
    List<OOrderStockingItemBatch> getBatchListByOrderStockingId(Long orderStockingId);
}
