# 启航零售POS - 第一期 TODO

> 目标：打通 Vue 前端主体流程，复用现有表，零新增表

## 整体流程

```
门店管理 → 营业员管理 → 商品管理 → 收银台 → 会员管理 → 销售单 → 报表
```

---

## TODO 列表（按优先级排序）

### P0 - 基础数据（先跑通）

- [x] **1. 执行数据库扩展脚本**
  - 文件: `docs/sql/pos_retail_extend_20260722.sql`
  - 扩展 o_shop / erp_sales_person / oms_shop_member / o_goods 字段

- [x] **2. 门店管理**
  - 后端: PosStoreController（复用 o_shop，type=999）
  - 前端: 门店列表/新增/编辑页面
  - 字段: 门店名称/编码/地址/电话/营业时间/状态

- [x] **3. 营业员管理**
  - 后端: PosEmployeeController（复用 erp_sales_person）
  - 前端: 营业员列表/新增/编辑页面
  - 字段: 姓名/手机号/工号/岗位/所属门店/提成比例

### P1 - 核心收银流程

- [x] **4. 商品列表（收银用）**
  - 后端: PosGoodsController（复用 o_goods + o_goods_sku）
  - 接口: 按条码查询 / 按名称搜索 / 分类列表
  - 前端: 收银台商品搜索组件

- [x] **5. 会员查询**
  - 后端: PosMemberController（复用 oms_shop_member）
  - 接口: 按手机号查询 / 新增会员
  - 前端: 收银台会员搜索组件

- [x] **6. 折扣规则查询**
  - 后端: PosPromotionController（复用 o_marketing_discount_rule）
  - 接口: 查询当前生效的折扣规则
  - 前端: 自动计算折扣

- [ ] **7. POS收银台**
  - 后端: PosCashierController
  - 接口: 提交销售单（复用 erp_sales_order + erp_sales_order_item）
  - 前端: 收银台主界面（商品列表/购物车/结算）
  - 流程: 扫码→加购→选会员→自动折扣→选择支付方式→提交订单

### P2 - 完善功能

- [ ] **8. 销售单查询**
  - 后端: PosOrderController
  - 前端: 销售单列表/详情页面

- [ ] **9. 班次管理**
  - 后端: ShiftController
  - 前端: 开班/交班/班次记录

- [ ] **10. 销售报表**
  - 后端: ReportController
  - 接口: 日报/商品排行/员工业绩
  - 前端: 报表页面

---

## 当前进度

| 序号 | 任务 | 状态 | 说明 |
|:----:|------|:----:|------|
| 1 | 执行数据库扩展脚本 | ✅ 已完成 | SQL脚本已生成 |
| 2 | 门店管理 | ✅ 已完成 | PosStoreController + OShopService |
| 3 | 营业员管理 | ✅ 已完成 | PosEmployeeController + ErpSalesPersonService |
| 4 | 商品列表 | ✅ 已完成 | PosGoodsController |
| 5 | 会员查询 | ✅ 已完成 | PosMemberController + OmsShopMemberService |
| 6 | 折扣规则 | ✅ 已完成 | PosPromotionController + OMarketingDiscountRuleService |
| 7 | POS收银台 | ⏳ 待开发 | 需实现提交销售单逻辑 |
| 8 | 销售单查询 | ⏳ 待开发 | PosOrderController 已创建，需完善 |
| 9 | 班次管理 | ⏳ 待开发 | |
| 10 | 销售报表 | ⏳ 待开发 | |

---

## 已完成的文件

### 后端 Controller
- `erp-api/.../controller/pos/PosStoreController.java` - 门店管理
- `erp-api/.../controller/pos/PosEmployeeController.java` - 营业员管理
- `erp-api/.../controller/pos/PosGoodsController.java` - 商品查询
- `erp-api/.../controller/pos/PosMemberController.java` - 会员查询
- `erp-api/.../controller/pos/PosPromotionController.java` - 折扣规则
- `erp-api/.../controller/pos/PosCashierController.java` - 收银台（待完善）
- `erp-api/.../controller/pos/PosOrderController.java` - 销售单查询

### 后端 Service
- `service/.../OShopService.java` - 店铺服务接口
- `service/.../ErpSalesPersonService.java` - 营业员服务接口
- `service/.../OmsShopMemberService.java` - 会员服务接口
- `service/.../OMarketingDiscountRuleService.java` - 折扣规则服务接口
- `service/.../ErpSalesOrderService.java` - 销售单服务接口

### 后端 Service 实现
- `service/.../impl/OShopServiceImpl.java`
- `service/.../impl/ErpSalesPersonServiceImpl.java`
- `service/.../impl/OmsShopMemberServiceImpl.java`
- `service/.../impl/OMarketingDiscountRuleServiceImpl.java`
- `service/.../impl/ErpSalesOrderServiceImpl.java`

### 后端 Mapper
- `mapper/.../OShopMapper.java`
- `mapper/.../ErpSalesPersonMapper.java`
- `mapper/.../OmsShopMemberMapper.java`
- `mapper/.../OMarketingDiscountRuleMapper.java`
- `mapper/.../ErpSalesOrderMapper.java`
- `mapper/.../ErpSalesOrderItemMapper.java`

### 后端 Entity
- `model/.../entity/OShop.java`
- `model/.../entity/ErpSalesPerson.java`
- `model/.../entity/OmsShopMember.java`
- `model/.../entity/OMarketingDiscountRule.java`
- `model/.../entity/ErpSalesOrder.java`
- `model/.../entity/ErpSalesOrderItem.java`

### 数据库脚本
- `docs/sql/pos_retail_extend_20260722.sql` - ALTER TABLE 语句

### 枚举类
- `common/.../enums/EnumPayMethod.java` - 支付方式
- `common/.../enums/EnumStoreType.java` - 门店类型
- `common/.../enums/EnumShiftStatus.java` - 班次状态
- `common/.../enums/EnumMemberLevelCondition.java` - 会员等级条件
- `common/.../enums/EnumStoredLogType.java` - 储值流水类型
- `common/.../enums/EnumPointsLogType.java` - 积分流水类型
- `common/.../enums/EnumCouponType.java` - 优惠券类型
- `common/.../enums/EnumCouponIssueStatus.java` - 优惠券状态
- `common/.../enums/EnumDeliveryType.java` - 配送方式
- `common/.../enums/EnumCommissionType.java` - 提成类型
- `common/.../enums/EnumCommissionRecordStatus.java` - 提成记录状态
- `common/.../enums/EnumDeliveryOrderStatus.java` - 配送单状态
- `common/.../enums/EnumShelfDateType.java` - 保质期录入方式

### 全局异常处理
- `erp-api/.../config/GlobalExceptionHandler.java`

### 文档
- `docs/PRD-启航零售数字化系统.md` - PRD v3.0
- `docs/TODO-Phase1.md` - 本文件
