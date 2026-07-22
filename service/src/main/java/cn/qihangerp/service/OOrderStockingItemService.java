package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.OOrderStockingItem;
import cn.qihangerp.model.bo.StockingOrderItemBo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author qilip
* @description 针对表【o_supplier_ship_order_item】的数据库操作Service
* @createDate 2025-02-18 13:48:13
*/
public interface OOrderStockingItemService extends IService<OOrderStockingItem> {
    /**
     * 查询仓库备货清单
     * @param bo
     * @param pageQuery
     * @return
     */
    PageResult<OOrderStockingItem> queryStockingPageList(StockingOrderItemBo bo, PageQuery pageQuery);

}
