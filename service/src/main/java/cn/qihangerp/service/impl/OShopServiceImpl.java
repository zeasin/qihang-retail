package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.OShopMapper;
import cn.qihangerp.model.entity.OShop;
import cn.qihangerp.service.OShopService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 店铺服务实现
 */
@Service
public class OShopServiceImpl extends ServiceImpl<OShopMapper, OShop> implements OShopService {

    @Override
    public PageResult<OShop> queryPageList(OShop query, PageQuery pageQuery) {
        Page<OShop> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<OShop> wrapper = buildQueryWrapper(query);
        Page<OShop> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    @Override
    public List<OShop> queryList(OShop query) {
        LambdaQueryWrapper<OShop> wrapper = buildQueryWrapper(query);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public OShop selectShopById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int insertShop(OShop shop) {
        return baseMapper.insert(shop);
    }

    @Override
    public int updateShop(OShop shop) {
        return baseMapper.updateById(shop);
    }

    @Override
    public int deleteShopByIds(Long[] ids) {
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }

    private LambdaQueryWrapper<OShop> buildQueryWrapper(OShop query) {
        LambdaQueryWrapper<OShop> wrapper = new LambdaQueryWrapper<>();
        if (query.getType() != null) {
            wrapper.eq(OShop::getType, query.getType());
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(OShop::getName, query.getName());
        }
        if (query.getMerchantId() != null) {
            wrapper.eq(OShop::getMerchantId, query.getMerchantId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(OShop::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(OShop::getSort);
        return wrapper;
    }
}
