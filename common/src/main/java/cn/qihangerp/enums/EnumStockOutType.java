package cn.qihangerp.enums;

/**
 * 描述：发货状态
 *
 *
 * @author qlp
 * @date 2019-09-18 19:44
 */
public enum EnumStockOutType {
    ORDER_STOCK_OUT("订单发货出库", 1),
    PUR_RET_STOCK_OUT("采购退货出库", 2),
    TAKE_STOCK_OUT("盘亏出库", 3),
    BAD_STOCK_OUT("报损出库", 4)
    ;
    private String name;
    private int index;

    // 构造方法
    private EnumStockOutType(String name, int index) {
        this.name = name;
        this.index = index;
    }

    // 普通方法
    public static String getName(int index) {
        for (EnumStockOutType c : EnumStockOutType.values()) {
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
