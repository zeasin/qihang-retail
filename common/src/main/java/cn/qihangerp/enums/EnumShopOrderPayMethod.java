package cn.qihangerp.enums;

public enum EnumShopOrderPayMethod {
    NONE("未知", "NONE"),
    XYHF("先用后付", "XYHF"),
    ALIPAY("支付宝", "ALIPAY"),
    REWARD("奖品订单", "REWARD"),
    POINTS("积分兑换", "POINTS"),
    APPLE("apple", "APPLE"),
    YUNSANFU("云闪付", "YUNSANFU"),
    WEIXIN("微信支持", "WEIXIN")
    ;
    private String name;
    private String code;

    // 构造方法
    private EnumShopOrderPayMethod(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // 普通方法
    public static String getName(String code) {
        for (EnumShopOrderPayMethod c : EnumShopOrderPayMethod.values()) {
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
