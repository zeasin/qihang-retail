package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付方式枚举
 */
@Getter
@AllArgsConstructor
public enum EnumPayMethod {

    CASH(1, "现金"),
    WECHAT(2, "微信"),
    ALIPAY(3, "支付宝"),
    BANK_CARD(4, "银行卡"),
    MEMBER_BALANCE(5, "会员余额"),
    AGGREGATE_CODE(6, "聚合扫码"),
    OTHER(7, "其他");

    private final int code;
    private final String name;

    public static EnumPayMethod fromCode(int code) {
        for (EnumPayMethod method : values()) {
            if (method.code == code) {
                return method;
            }
        }
        throw new IllegalArgumentException("未知支付方式: " + code);
    }
}
