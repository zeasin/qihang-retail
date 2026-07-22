package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 优惠券发放状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnumCouponIssueStatus {

    UNUSED(1, "未使用"),
    USED(2, "已使用"),
    EXPIRED(3, "已过期"),
    CANCELLED(4, "已作废");

    private final int code;
    private final String name;

    public static EnumCouponIssueStatus fromCode(int code) {
        for (EnumCouponIssueStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知优惠券状态: " + code);
    }
}
