package cn.qihangerp.enums;

import java.io.Serializable;

public enum EnumOrderSource implements Serializable {
    POS("POS收银", "POS"),
    MT_FLASH("美团闪购", "MT_FLASH"),
    MT_WM("美团外卖", "MT_WM"),
    JD_DJ("京东到家", "JD_DJ"),
    TAO_FLASH("淘宝闪购", "TAO_FLASH"),
    EL_FLASH("饿了么", "EL_FLASH"),
    WECHAT_MINI("微信小程序", "WECHAT_MINI"),
    OTHER("其他", "OTHER")
    ;

    private String name;
    private String code;

    private EnumOrderSource(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static String getName(String code) {
        if (code == null) return null;
        for (EnumOrderSource c : EnumOrderSource.values()) {
            if (c.getCode().equals(code)) {
                return c.name;
            }
        }
        return null;
    }

    public static EnumOrderSource getByCode(String code) {
        if (code == null) return null;
        for (EnumOrderSource source : EnumOrderSource.values()) {
            if (source.getCode().equals(code)) {
                return source;
            }
        }
        return null;
    }

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
