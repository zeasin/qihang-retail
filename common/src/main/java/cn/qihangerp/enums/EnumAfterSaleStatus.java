package cn.qihangerp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 售后状态枚举（erp_status）
 */
@Getter
@AllArgsConstructor
public enum EnumAfterSaleStatus {

    PENDING_AUDIT(0, "待审核"),
    PENDING_RETURN(1, "待退货"),
    PENDING_REFUND(2, "待退款"),
    PENDING_EXCHANGE(3, "待换发"),
    COMPLETED(10, "已完成"),
    CANCELLED(11, "已取消"),
    REJECTED(12, "已拒绝");

    private final int code;
    private final String name;

    public static EnumAfterSaleStatus fromCode(int code) {
        for (EnumAfterSaleStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("未知售后状态: " + code);
    }
}
