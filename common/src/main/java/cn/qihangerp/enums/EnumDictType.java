package cn.qihangerp.enums;

/**
 * 描述：字典
 *
 *
 * @author qlp
 * @date
 */
public enum EnumDictType {
    GOODS_UNIT("商品单位", "goodsUnit"),
    AFTER_SALE_REASON_TYPE("售后问题分类", "refund_reason_type")
    ;
    private String name;
    private String code;

    // 构造方法
    private EnumDictType(String name, String code) {
        this.name = name;
        this.code = code;
    }

    // 普通方法
    public static String getName(String code) {
        for (EnumDictType c : EnumDictType.values()) {
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
