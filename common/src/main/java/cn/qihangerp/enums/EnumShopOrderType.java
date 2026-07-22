package cn.qihangerp.enums;

/**
 * 描述：订单类型：0普通订单，1螳螂电销订单，2螳螂网销订单
 *
 *
 * @author qlp
 * @date 2019-09-18 19:44
 */
public enum EnumShopOrderType {
    Pre_Sale_D("定金预售订单", 10),
    Pre_Sale_Q("全款预售订单", 20),
    HUAN_HUO_BU_FA("换货补发订单", 30),
    TL_DIAN("螳螂电销订单", 1),
    TL_WANG("螳螂网销订单", 2),
    Normal("普通订单", 0)
    ;
    private String name;
    private int index;

    // 构造方法
    private EnumShopOrderType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (EnumShopOrderType c : EnumShopOrderType.values()) {
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
