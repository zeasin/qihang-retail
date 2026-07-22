package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 会员等级升级条件枚举
 */
@Getter
@AllArgsConstructor
public enum EnumMemberLevelCondition {

    TOTAL_CONSUMPTION(1, "累计消费"),
    TOTAL_POINTS(2, "累计积分"),
    RECHARGE(3, "充值");

    private final int code;
    private final String name;

    public static EnumMemberLevelCondition fromCode(int code) {
        for (EnumMemberLevelCondition condition : values()) {
            if (condition.code == code) {
                return condition;
            }
        }
        throw new IllegalArgumentException("未知升级条件: " + code);
    }
}
