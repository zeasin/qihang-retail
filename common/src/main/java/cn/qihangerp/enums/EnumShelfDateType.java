package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 保质期录入方式枚举
 */
@Getter
@AllArgsConstructor
public enum EnumShelfDateType {

    NONE(0, "无需录入"),
    PRODUCTION_DATE_PLUS_DAYS(1, "生产日期+天数"),
    FIXED_EXPIRY_DATE(2, "固定到期日");

    private final int code;
    private final String name;

    public static EnumShelfDateType fromCode(int code) {
        for (EnumShelfDateType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知保质期录入方式: " + code);
    }
}
