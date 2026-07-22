package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提成类型枚举
 */
@Getter
@AllArgsConstructor
public enum EnumCommissionType {

    BY_SALES(1, "按销售额"),
    BY_CATEGORY(2, "按商品品类"),
    BY_RECHARGE(3, "按会员充值"),
    TIERED(4, "阶梯");

    private final int code;
    private final String name;

    public static EnumCommissionType fromCode(int code) {
        for (EnumCommissionType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知提成类型: " + code);
    }
}
