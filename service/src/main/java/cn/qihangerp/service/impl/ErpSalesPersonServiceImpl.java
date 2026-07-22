package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.ErpSalesPersonMapper;
import cn.qihangerp.model.entity.ErpSalesPerson;
import cn.qihangerp.service.ErpSalesPersonService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 营业员服务实现
 */
@Service
public class ErpSalesPersonServiceImpl extends ServiceImpl<ErpSalesPersonMapper, ErpSalesPerson> implements ErpSalesPersonService {

    @Override
    public PageResult<ErpSalesPerson> queryPageList(ErpSalesPerson query, PageQuery pageQuery) {
        Page<ErpSalesPerson> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<ErpSalesPerson> wrapper = buildQueryWrapper(query);
        Page<ErpSalesPerson> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    @Override
    public List<ErpSalesPerson> queryList(ErpSalesPerson query) {
        LambdaQueryWrapper<ErpSalesPerson> wrapper = buildQueryWrapper(query);
        return baseMapper.selectList(wrapper);
    }

    private LambdaQueryWrapper<ErpSalesPerson> buildQueryWrapper(ErpSalesPerson query) {
        LambdaQueryWrapper<ErpSalesPerson> wrapper = new LambdaQueryWrapper<>();
        if (query.getMerchantId() != null) {
            wrapper.eq(ErpSalesPerson::getMerchantId, query.getMerchantId());
        }
        if (query.getShopId() != null) {
            wrapper.eq(ErpSalesPerson::getShopId, query.getShopId());
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(ErpSalesPerson::getName, query.getName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(ErpSalesPerson::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(ErpSalesPerson::getCreatedTime);
        return wrapper;
    }
}
