package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 门店类型枚举
 */
@Getter
@AllArgsConstructor
public enum EnumStoreType {

    DIRECT(1, "直营"),
    FRANCHISE(2, "加盟");

    private final int code;
    private final String name;

    public static EnumStoreType fromCode(int code) {
        for (EnumStoreType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知门店类型: " + code);
    }
}
