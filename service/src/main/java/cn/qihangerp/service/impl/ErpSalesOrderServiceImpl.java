package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.ErpSalesOrderItemMapper;
import cn.qihangerp.mapper.ErpSalesOrderMapper;
import cn.qihangerp.model.entity.ErpSalesOrder;
import cn.qihangerp.model.entity.ErpSalesOrderItem;
import cn.qihangerp.service.ErpSalesOrderService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 销售单服务实现
 */
@Service
public class ErpSalesOrderServiceImpl extends ServiceImpl<ErpSalesOrderMapper, ErpSalesOrder> implements ErpSalesOrderService {

    private final ErpSalesOrderItemMapper orderItemMapper;

    public ErpSalesOrderServiceImpl(ErpSalesOrderItemMapper orderItemMapper) {
        this.orderItemMapper = orderItemMapper;
    }

    @Override
    public PageResult<ErpSalesOrder> queryPageList(ErpSalesOrder query, PageQuery pageQuery) {
        Page<ErpSalesOrder> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<ErpSalesOrder> wrapper = buildQueryWrapper(query);
        Page<ErpSalesOrder> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    @Override
    public List<ErpSalesOrderItem> selectItemsByOrderId(Long orderId) {
        LambdaQueryWrapper<ErpSalesOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ErpSalesOrderItem::getOrderId, orderId);
        return orderItemMapper.selectList(wrapper);
    }

    private LambdaQueryWrapper<ErpSalesOrder> buildQueryWrapper(ErpSalesOrder query) {
        LambdaQueryWrapper<ErpSalesOrder> wrapper = new LambdaQueryWrapper<>();
        if (query.getMerchantId() != null) {
            wrapper.eq(ErpSalesOrder::getMerchantId, query.getMerchantId());
        }
        if (query.getShopId() != null) {
            wrapper.eq(ErpSalesOrder::getShopId, query.getShopId());
        }
        if (StringUtils.hasText(query.getOrderNo())) {
            wrapper.like(ErpSalesOrder::getOrderNo, query.getOrderNo());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ErpSalesOrder::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ErpSalesOrder::getCreateTime);
        return wrapper;
    }
}
