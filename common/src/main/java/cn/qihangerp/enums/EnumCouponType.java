package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券类型枚举
 */
@Getter
@AllArgsConstructor
public enum EnumCouponType {

    FULL_REDUCTION(1, "满减券"),
    DISCOUNT(2, "折扣券"),
    CATEGORY(3, "品类券"),
    GOODS(4, "商品券"),
    BIRTHDAY(5, "生日券"),
    NO_THRESHOLD(6, "无门槛券");

    private final int code;
    private final String name;

    public static EnumCouponType fromCode(int code) {
        for (EnumCouponType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知优惠券类型: " + code);
    }
}
