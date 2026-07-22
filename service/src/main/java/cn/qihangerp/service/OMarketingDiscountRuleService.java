package cn.qihangerp.service;

import cn.qihangerp.model.entity.OMarketingDiscountRule;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 折扣规则服务接口
 */
public interface OMarketingDiscountRuleService extends IService<OMarketingDiscountRule> {

    /**
     * 查询当前生效的折扣规则
     */
    List<OMarketingDiscountRule> queryActiveRules(Long shopId);

    /**
     * 查询指定商品适用的折扣规则
     */
    List<OMarketingDiscountRule> queryRulesByGoods(Long goodsId, Long shopId);
}
