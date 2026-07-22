package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 积分流水类型枚举
 */
@Getter
@AllArgsConstructor
public enum EnumPointsLogType {

    EARN_BY_CONSUMPTION(1, "消费获得"),
    DEDUCT_BY_EXCHANGE(2, "兑换扣除"),
    EXPIRE(3, "过期"),
    MANUAL_ADJUST(4, "手动调整"),
    REGISTER_REWARD(5, "注册奖励");

    private final int code;
    private final String name;

    public static EnumPointsLogType fromCode(int code) {
        for (EnumPointsLogType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知积分流水类型: " + code);
    }
}
