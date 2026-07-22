package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.OMarketingDiscountRule;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OMarketingDiscountRuleService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 折扣规则查询Controller（POS收银用）
 * 复用 o_marketing_discount_rule 表
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/promotion")
public class PosPromotionController extends BaseController {

    private final OMarketingDiscountRuleService discountRuleService;

    /**
     * 查询当前生效的折扣规则
     */
    @GetMapping("/active")
    public AjaxResult getActiveRules(@RequestParam Long shopId) {
        List<OMarketingDiscountRule> rules = discountRuleService.queryActiveRules(shopId);
        return success(rules);
    }

    /**
     * 查询指定商品适用的折扣规则
     */
    @GetMapping("/goods/{goodsId}")
    public AjaxResult getRulesByGoods(@PathVariable Long goodsId, @RequestParam Long shopId) {
        List<OMarketingDiscountRule> rules = discountRuleService.queryRulesByGoods(goodsId, shopId);
        return success(rules);
    }

    /**
     * 计算订单折扣（收银时调用）
     */
    @PostMapping("/calculate")
    public AjaxResult calculateDiscount(@RequestBody DiscountRequest request) {
        // TODO: 实现折扣计算逻辑
        // 1. 查询适用的折扣规则
        // 2. 按优先级排序
        // 3. 计算最优折扣组合
        return success("折扣计算功能待实现");
    }

    /**
     * 折扣计算请求
     */
    @lombok.Data
    public static class DiscountRequest {
        private Long shopId;
        private Long memberId;
        private java.math.BigDecimal totalAmount;
        private List<Long> goodsIds;
    }
}
