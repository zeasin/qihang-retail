package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 班次状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnumShiftStatus {

    IN_PROGRESS(0, "进行中"),
    COMPLETED(1, "已交班");

    private final int code;
    private final String name;

    public static EnumShiftStatus fromCode(int code) {
        for (EnumShiftStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知班次状态: " + code);
    }
}
