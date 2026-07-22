package cn.qihangerp.enums;

/**
 * API接口类型
 *
 *
 * @author qlp
 * @date 2025-09-28 19:44
 */
public enum EnumShopApiStatus {
    OPEN("平台接口", 1),
    SHOP("店铺接口", 2),
    DIANSAN("点三接口拉取", 11),
    JKY("吉客云接口", 21),
    CLOSED("禁用", 0)
    ;
    private String name;
    private int index;

    // 构造方法
    private EnumShopApiStatus(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (EnumShopApiStatus c : EnumShopApiStatus.values()) {
            if (c.getIndex() == index) {
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
