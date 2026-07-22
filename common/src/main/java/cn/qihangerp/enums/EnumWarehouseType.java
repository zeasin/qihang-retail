package cn.qihangerp.enums;

/**
 * 描述：
 * 店铺类型Enum
 *
 * @author qlp
 * @date 2019-09-18 19:44
 */
public enum EnumWarehouseType {
    CLOUD("系统云仓", "CLOUD"),
    JDYC("京东云仓", "JDYC"),
    JKYYC("吉客云云仓", "JKYYC"),
    SUPPLIER("供应商仓库", "SUPPLIER"),
    SHOP("门店虚拟仓", "SHOP"),
    LOCAL("本地仓", "LOCAL")
    ;
    private String name;
    private String type;

    // 构造方法
    private EnumWarehouseType(String name, String type) {
        this.name = name;
        this.type = type;
    }

    // 普通方法
    public static String getName(String type) {
        for (EnumWarehouseType c : EnumWarehouseType.values()) {
            if (c.getType().equals(type)) {
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

    public String getType() {
        return type;
    }

    public void setIndex(String type) {
        this.type = type;
    }
}
