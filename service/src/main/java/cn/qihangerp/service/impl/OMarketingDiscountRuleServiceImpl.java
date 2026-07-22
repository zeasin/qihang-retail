package cn.qihangerp.service.impl;

import cn.qihangerp.mapper.OMarketingDiscountRuleMapper;
import cn.qihangerp.model.entity.OMarketingDiscountRule;
import cn.qihangerp.service.OMarketingDiscountRuleService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 折扣规则服务实现
 */
@Service
public class OMarketingDiscountRuleServiceImpl extends ServiceImpl<OMarketingDiscountRuleMapper, OMarketingDiscountRule> implements OMarketingDiscountRuleService {

    @Override
    public List<OMarketingDiscountRule> queryActiveRules(Long shopId) {
        long now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LambdaQueryWrapper<OMarketingDiscountRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OMarketingDiscountRule::getStatus, 1); // 启用状态
        wrapper.le(OMarketingDiscountRule::getStartTime, now);
        wrapper.ge(OMarketingDiscountRule::getEndTime, now);
        // 查询适用当前门店的规则（apply_scope=1全部 或 apply_scope=3且apply_shop_id=shopId）
        wrapper.and(w -> w.eq(OMarketingDiscountRule::getApplyScope, 1)
                .or(q -> q.eq(OMarketingDiscountRule::getApplyScope, 3)
                        .eq(OMarketingDiscountRule::getApplyShopId, shopId)));
        wrapper.orderByDesc(OMarketingDiscountRule::getPriority);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public List<OMarketingDiscountRule> queryRulesByGoods(Long goodsId, Long shopId) {
        // TODO: 实现按商品查询折扣规则
        // 目前先返回当前门店的所有生效规则
        return queryActiveRules(shopId);
    }
}
