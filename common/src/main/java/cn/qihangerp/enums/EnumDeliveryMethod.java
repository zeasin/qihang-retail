package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送方式枚举（线下零售场景）
 * 对应 o_order.delivery_method 字段
 */
@Getter
@AllArgsConstructor
public enum EnumDeliveryMethod {

    CASH(1, "现结"),
    PICKUP(2, "到店自提"),
    MERCHANT(3, "商家配送"),
    RIDER(4, "骑手配送");

    private final int code;
    private final String name;

    public static EnumDeliveryMethod fromCode(int code) {
        for (EnumDeliveryMethod method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知配送方式: " + code);
    }
}
