package cn.qihangerp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 折扣规则实体类
 * 对应表: o_marketing_discount_rule
 */
@Data
@TableName("o_marketing_discount_rule")
public class OMarketingDiscountRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 折扣规则名称
     */
    private String ruleName;

    /**
     * 折扣类型：1-百分比折扣，2-固定金额折扣，3-满减，4-第二件半价，5-限时秒杀
     */
    private Integer discountType;

    /**
     * 折扣值
     */
    private BigDecimal discountValue;

    /**
     * 适用范围：1-全部，2-商户，3-门店
     */
    private Integer applyScope;

    /**
     * 适用商户ID
     */
    private Long applyMerchantId;

    /**
     * 适用商户名
     */
    private String applyMerchantName;

    /**
     * 适用店铺ID
     */
    private Long applyShopId;

    /**
     * 适用店铺名称
     */
    private String applyShopName;

    /**
     * 总可用次数，0表示不限次数
     */
    private Integer totalQuota;

    /**
     * 已使用次数
     */
    private Integer usedQuota;

    /**
     * 创建来源：1-总部，2-商户，3-店铺
     */
    private Integer sourceType;

    /**
     * 来源ID
     */
    private Long sourceId;

    /**
     * 订单金额下限
     */
    private BigDecimal minOrderAmount;

    /**
     * 生效开始时间（毫秒时间戳）
     */
    private Long startTime;

    /**
     * 生效结束时间（毫秒时间戳）
     */
    private Long endTime;

    /**
     * 状态：0待审核，1-启用，2-审核拒绝
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 创建人标识
     */
    private String createdBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 备注说明
     */
    private String remark;
}
