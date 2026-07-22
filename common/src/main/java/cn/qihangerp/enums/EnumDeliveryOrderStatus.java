package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配送单状态枚举
 */
@Getter
@AllArgsConstructor
public enum EnumDeliveryOrderStatus {

    PENDING_CALL(0, "待呼叫"),
    CALLING(1, "呼叫中"),
    DRIVER_ACCEPTED(2, "骑手已接单"),
    PICKED_UP(3, "已取件"),
    DELIVERING(4, "配送中"),
    DELIVERED(5, "已送达"),
    EXCEPTION(6, "异常"),
    CANCELLED(7, "取消");

    private final int code;
    private final String name;

    public static EnumDeliveryOrderStatus fromCode(int code) {
        for (EnumDeliveryOrderStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知配送单状态: " + code);
    }
}
