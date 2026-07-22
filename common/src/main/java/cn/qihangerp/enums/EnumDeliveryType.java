package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送方式枚举
 */
@Getter
@AllArgsConstructor
public enum EnumDeliveryType {

    PLATFORM(0, "平台配送"),
    SELF(1, "商家自配"),
    PICKUP(2, "到店自提");

    private final int code;
    private final String name;

    public static EnumDeliveryType fromCode(int code) {
        for (EnumDeliveryType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知配送方式: " + code);
    }
}
