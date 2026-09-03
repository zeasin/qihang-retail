package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.ORefundMapper;
import cn.qihangerp.model.entity.ORefund;
import cn.qihangerp.service.ORefundService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 售后退款服务实现
 */
@Service
public class ORefundServiceImpl extends ServiceImpl<ORefundMapper, ORefund> implements ORefundService {

    @Override
    public PageResult<ORefund> queryPageList(ORefund query, PageQuery pageQuery) {
        Page<ORefund> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<ORefund> wrapper = buildQueryWrapper(query);
        Page<ORefund> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    private LambdaQueryWrapper<ORefund> buildQueryWrapper(ORefund query) {
        LambdaQueryWrapper<ORefund> wrapper = new LambdaQueryWrapper<>();
        if (query.getShopId() != null) {
            wrapper.eq(ORefund::getShopId, query.getShopId());
        }
        if (query.getShopType() != null) {
            wrapper.eq(ORefund::getShopType, query.getShopType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ORefund::getStatus, query.getStatus());
        }
        if (query.getRefundType() != null) {
            wrapper.eq(ORefund::getRefundType, query.getRefundType());
        }
        if (StringUtils.hasText(query.getOrderNum())) {
            wrapper.like(ORefund::getOrderNum, query.getOrderNum());
        }
        if (StringUtils.hasText(query.getRefundNum())) {
            wrapper.like(ORefund::getRefundNum, query.getRefundNum());
        }
        if (StringUtils.hasText(query.getGoodsName())) {
            wrapper.like(ORefund::getGoodsName, query.getGoodsName());
        }
        wrapper.orderByDesc(ORefund::getCreateTime);
        return wrapper;
    }
}
