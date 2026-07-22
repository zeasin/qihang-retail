package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 提成记录状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnumCommissionRecordStatus {

    PENDING(0, "待计算"),
    CALCULATED(1, "已计算"),
    PAID(2, "已发放"),
    CANCELLED(3, "已作废");

    private final int code;
    private final String name;

    public static EnumCommissionRecordStatus fromCode(int code) {
        for (EnumCommissionRecordStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知提成记录状态: " + code);
    }
}
