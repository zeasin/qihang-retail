package cn.qihangerp.enums;

/**
 * 描述：发货状态
 *
 *
 * @author qlp
 * @date 2019-09-18 19:44
 */
public enum EnumStockInType {
    PUR_STOCK_IN("采购入库", 1),
    RET_STOCK_IN("退货入库", 2),
    TAKE_STOCK_IN("盘盈入库", 3),
    OTHER("其他未分类", 0)
    ;
    private String name;
    private int index;

    // 构造方法
    private EnumStockInType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (EnumStockInType c : EnumStockInType.values()) {
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
