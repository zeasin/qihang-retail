package cn.qihangerp.enums;

/**
 * 描述：店铺订单状态
 *
 *
 * @author qlp
 * @date
 */
public enum EnumShopOrderStatus {
    /**
     * 订单状态0：新订单，1：待发货，2：已发货，3：已完成，11已取消；12退款中；13已关闭；21待付款；22锁定，29删除，101部分发货
     */
    WAIT_SHIP("待发货", "WAIT_SHIP",1),
    PART_SHIP("部分发货", "PART_SHIP",101),
    SHIPPED("已发货", "SHIPPED",2),
    COMPLETE("已完成", "COMPLETE",3),
    CANCEL("已取消", "COMPLETE",11),
    REFUND("退款中", "REFUND",12),
    CLOSED("已关闭", "CLOSED",13),
    WAIT_PAY("待付款", "WAIT_PAY",21),
    LOCKED("锁定", "LOCKED",22),
    DELETED("删除", "DELETED",29),
    NEW("新订单", "NEW",0)
    ;
    private String name;
    private String code;
    private Integer status;

    // 构造方法
    private EnumShopOrderStatus(String name, String code,Integer status) {
        this.name = name;
        this.code = code;
        this.status = status;
    }

    // 普通方法
    public static String getName(String code) {
        for (EnumShopOrderStatus c : EnumShopOrderStatus.values()) {
            if (code.equals(c.getCode())) {
                return c.name;
            }
        }
        return null;
    }
    // 普通方法
    public static String getName(Integer status) {
        for (EnumShopOrderStatus c : EnumShopOrderStatus.values()) {
            if (status==c.status) {
                return c.name;
            }
        }
        return null;
    }

    public static String getCode(Integer status) {
        for (EnumShopOrderStatus c : EnumShopOrderStatus.values()) {
            if (status==c.status) {
                return c.code;
            }
        }
        return null;
    }
    public static Integer getStatus(String code) {
        for (EnumShopOrderStatus c : EnumShopOrderStatus.values()) {
            if (code.equals(c.getCode())) {
                return c.status;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
