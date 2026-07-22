package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.mapper.OmsShopMemberMapper;
import cn.qihangerp.model.entity.OmsShopMember;
import cn.qihangerp.service.OmsShopMemberService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 会员服务实现
 */
@Service
public class OmsShopMemberServiceImpl extends ServiceImpl<OmsShopMemberMapper, OmsShopMember> implements OmsShopMemberService {

    @Override
    public OmsShopMember queryByPhone(String phone, Long shopId) {
        LambdaQueryWrapper<OmsShopMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsShopMember::getPhone, phone);
        if (shopId != null) {
            wrapper.eq(OmsShopMember::getShopId, shopId);
        }
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public boolean existsByPhone(String phone, Long merchantId) {
        LambdaQueryWrapper<OmsShopMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OmsShopMember::getPhone, phone);
        wrapper.eq(OmsShopMember::getMerchantId, merchantId);
        return baseMapper.selectCount(wrapper) > 0;
    }

    @Override
    public PageResult<OmsShopMember> queryPageList(OmsShopMember query, PageQuery pageQuery) {
        Page<OmsShopMember> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
        LambdaQueryWrapper<OmsShopMember> wrapper = buildQueryWrapper(query);
        Page<OmsShopMember> result = baseMapper.selectPage(page, wrapper);
        return PageResult.build(result);
    }

    private LambdaQueryWrapper<OmsShopMember> buildQueryWrapper(OmsShopMember query) {
        LambdaQueryWrapper<OmsShopMember> wrapper = new LambdaQueryWrapper<>();
        if (query.getMerchantId() != null) {
            wrapper.eq(OmsShopMember::getMerchantId, query.getMerchantId());
        }
        if (query.getShopId() != null) {
            wrapper.eq(OmsShopMember::getShopId, query.getShopId());
        }
        if (StringUtils.hasText(query.getPhone())) {
            wrapper.like(OmsShopMember::getPhone, query.getPhone());
        }
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(OmsShopMember::getName, query.getName());
        }
        wrapper.orderByDesc(OmsShopMember::getCreateOn);
        return wrapper;
    }
}
