package cn.qihangerp.enums;

/**
 * 描述：
 *
 *
 * @author qlp
 * @date
 */
public enum EnumShopRefundPhase {
    //退款阶段, ON_SALE 售中、AFTER_SALE 售后
    ON_SALE("售中", "ON_SALE"),
    AFTER_SALE("售后", "AFTER_SALE")
    ;
    private String name;
    private String code;

    // 构造方法
    private EnumShopRefundPhase(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // 普通方法
    public static String getName(String code) {
        for (EnumShopRefundPhase c : EnumShopRefundPhase.values()) {
            if (code.equals(c.getCode())) {
                return c.name;
            }
        }
        return null;
    }

    // get set 方法
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
