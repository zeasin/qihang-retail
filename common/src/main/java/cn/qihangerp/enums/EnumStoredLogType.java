package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 储值流水类型枚举
 */
@Getter
@AllArgsConstructor
public enum EnumStoredLogType {

    RECHARGE(1, "充值"),
    CONSUME(2, "消费"),
    GIFT(3, "赠送"),
    REFUND(4, "退款"),
    ADJUST(5, "调整"),
    FREEZE(6, "冻结"),
    UNFREEZE(7, "解冻");

    private final int code;
    private final String name;

    public static EnumStoredLogType fromCode(int code) {
        for (EnumStoredLogType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知储值流水类型: " + code);
    }
}
