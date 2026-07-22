package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.mapper.*;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.bo.StockingOrderItemBo;
import cn.qihangerp.utils.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qihangerp.service.OOrderStockingItemService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

/**
* @author qilip
* @description 针对表【o_supplier_ship_order_item】的数据库操作Service实现
* @createDate 2025-02-18 13:48:13
*/
@AllArgsConstructor
@Service
public class OOrderStockingItemServiceImpl extends ServiceImpl<OOrderStockingItemMapper, OOrderStockingItem>
    implements OOrderStockingItemService {
    private final OOrderStockingMapper stockingMapper;
    private final OOrderStockingItemMapper stockingItemMapper;
    private final ErpStockOutItemMapper stockOutItemMapper;
    private final ErpStockOutMapper stockOutMapper;
    private final ErpWarehouseMapper warehouseMapper;

    @Override
    public PageResult<OOrderStockingItem> queryStockingPageList(StockingOrderItemBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OOrderStockingItem> queryWrapper = new LambdaQueryWrapper<OOrderStockingItem>()
                .eq(OOrderStockingItem::getSupplierId,0)
                .eq(OOrderStockingItem::getMerchantId,bo.getMerchantId())
                .eq(StringUtils.hasText(bo.getOrderNum()), OOrderStockingItem::getOrderNum,bo.getOrderNum())
                .eq(StringUtils.hasText(bo.getGoodsNum()), OOrderStockingItem::getGoodsNum,bo.getGoodsNum())
                .eq(StringUtils.hasText(bo.getSkuCode()), OOrderStockingItem::getSkuCode,bo.getSkuCode())
                .eq(bo.getStockingStatus()!=null, OOrderStockingItem::getStockingStatus,bo.getStockingStatus())
//                .eq(bo.getStockingStatus()==null, OOrderStockingItem::getStockingStatus,0)
                ;

        pageQuery.setOrderByColumn("id");
        pageQuery.setIsAsc("desc");
        Page<OOrderStockingItem> pages = stockingItemMapper.selectPage(pageQuery.build(), queryWrapper);

        return PageResult.build(pages);
    }

}




