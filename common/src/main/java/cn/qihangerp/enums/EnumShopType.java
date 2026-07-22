package cn.qihangerp.enums;

import java.io.Serializable;

/**
 * 描述：
 * 店铺类型Enum
 *
 * @author qlp
 * @date 2019-09-18 19:44
 */
public enum EnumShopType implements Serializable {
    TAO("淘宝天猫", 100),
    JD("京东POP", 200),
    JDVC("京东自营", 280),
    PDD("拼多多", 300),
    DOU("抖店", 400),
    WEI("微信小店", 500),
    KWAI("快手小店", 600),
    XHS("小红书", 700),
    WEI_DIAN("微店", 901),
    TANG_LANG("螳螂系统", 911),
    OFFLINE("线下渠道", 999),
    ERP_ORDER("ERP内销订单", 0),
    SHOP_ORDER("店铺订单", 10000)
    ;
    private String name;
    private int index;

    // 构造方法
    private EnumShopType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (EnumShopType c : EnumShopType.values()) {
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

    // 通过index获取枚举的静态方法
    public static EnumShopType getByIndex(int index) {
        for (EnumShopType shopType : EnumShopType.values()) {
            if (shopType.getIndex() == index) {
                return shopType;
            }
        }
        return null; // 或者抛出异常
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
