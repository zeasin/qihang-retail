package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后类型枚举（refund_type）
 */
@Getter
@AllArgsConstructor
public enum EnumAfterSaleType {

    RETURN_REFUND(10, "退货退款"),
    REFUND_ONLY(11, "仅退款"),
    EXCHANGE(20, "换货");

    private final int code;
    private final String name;

    public static EnumAfterSaleType fromCode(int code) {
        for (EnumAfterSaleType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知售后类型: " + code);
    }
}
