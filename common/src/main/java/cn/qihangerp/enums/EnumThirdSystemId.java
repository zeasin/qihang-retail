package cn.qihangerp.enums;

/**
 * 描述：
 * 店铺类型Enum
 *
 * @author qlp
 * @date 2019-09-18 19:44
 */
public enum EnumThirdSystemId {
    JDYC("京东云仓", 300),
    FEISHU("飞书", 201),
    DIANSAN("点三", 400),
    JACKYUN("吉客云", 500),

    internal("内部系统", 100)
    ;
    private String name;
    private int index;

    // 构造方法
    private EnumThirdSystemId(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (EnumThirdSystemId c : EnumThirdSystemId.values()) {
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
