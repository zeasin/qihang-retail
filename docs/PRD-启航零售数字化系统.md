# 启航零售数字化系统 — 产品需求文档（PRD）

| 项 | 内容 |
|---|---|
| 文档版本 | v3.0 |
| 创建日期 | 2026-07-22 |
| 产品代号 | qihang-retail |
| 文档状态 | 草案，待评审 |
| 适用范围 | 开源版 + 商业版 |

> **修订说明（v3.0）**：基于现有 `qihang-retail` SQL 表结构（161张表）重新设计，**最大化复用现有表，零新增表**，先跑通业务再迭代。

---

## 一、项目背景与目标

### 1.1 项目背景

线下零售门店（商超、便利、母婴、生鲜、烟酒、文具、3C数码等）正经历数字化转型。消费者对即时零售（美团闪购、淘宝闪购、京东到家、抖音小时达）的需求持续增长，但门店日常经营仍以**线下POS收银**为绝对核心。多数中小门店当前面临：

- 老旧收银系统功能单一、对账困难、不支持现代支付方式
- 商品/库存手工账，与即时零售平台库存割裂，频繁超卖
- 会员资产沉淀在收银本上，无法跨店跨渠道使用
- 即时零售平台订单依赖人工切换后台处理，漏单率高
- SaaS系统年费高、数据不在自己手里、被平台锁定

### 1.2 产品目标

打造一套**开源、可下载即用、功能完整**的零售数字化系统，覆盖：

- **线下POS全场景**：收银、会员、营销、财务、报表
- **即时零售**：对接美团闪购、淘宝闪购、京东到家、抖音小时达，订单/库存/商品统一管理

开源版面向**单店 / 连锁小店**，下载即用、零年费；商业版面向**连锁型大企业**，扩展加盟体系、业财一体、开放API。

### 1.3 架构复用策略

**最大化复用现有表**。现有 `qihang-retail` 代码库已有 161 张表，覆盖会员、折扣、门店、销售员、库存、采购、财务等全链路。**第一期目标：零新增表，先跑通业务**。

---

## 二、现有代码资产盘点（161张表）

### 2.1 核心复用表（POS收银直接可用）

| 现有表 | 现有功能 | POS收银用途 | 可用性 |
|---|---|---|---|
| `o_shop` | 店铺（type=999为线下门店） | **门店档案**（已有 province/city/district/address/contact/phone） | ✅ 直接用 |
| `erp_sales_person` | 销售人员（name/mobile/employee_no/commission_rate） | **营业员/收银员** | ✅ 直接用 |
| `oms_shop_member` | 会员（phone/name/province/city/address） | **会员档案** | ✅ 直接用 |
| `o_marketing_discount_rule` | 折扣规则（百分比/固定金额/适用范围/时段） | **促销折扣** | ✅ 直接用 |
| `o_goods` | 商品库（bar_code/retail_price/pur_price/is_weighted等） | **POS商品** | ✅ 直接用 |
| `o_goods_sku` | SKU管理（bar_code/price/cost_price/spec_info） | **POS SKU** | ✅ 直接用 |
| `o_goods_category` | 商品分类（parent_id/path/merchant_id） | **商品分类** | ✅ 直接用 |
| `o_goods_brand` | 品牌管理 | **商品品牌** | ✅ 直接用 |
| `o_goods_inventory` | 库存（quantity/locked_quantity/available_quantity） | **库存查询** | ✅ 直接用 |
| `o_goods_inventory_batch` | 批次库存（production_date/period） | **保质期管理** | ✅ 直接用 |
| `erp_stock_in` / `erp_stock_out` | 出入库单 | **采购入库/销售出库** | ✅ 直接用 |
| `erp_purchase_order` | 采购订单 | **采购管理** | ✅ 直接用 |
| `erp_supplier` | 供应商 | **供应商管理** | ✅ 直接用 |
| `erp_warehouse` | 仓库（merchant_id/shop_id/warehouse_type） | **门店仓库** | ✅ 直接用 |
| `erp_sales_order` | 销售订单 | **POS销售单** | ✅ 直接用 |
| `erp_sales_order_item` | 销售明细 | **POS销售明细** | ✅ 直接用 |
| `sys_user` / `sys_role` / `sys_menu` | 用户/角色/菜单 | **权限管理** | ✅ 直接用 |
| `fms_finance_ledger` | 财务日记账 | **对账记账** | ✅ 直接用 |

### 2.2 可扩展表（新增少量字段即可）

| 现有表 | 需扩展字段 | 用途 |
|---|---|---|
| `o_goods` | `is_weighted` TINYINT DEFAULT 0 | 是否称重商品 |
| `o_goods` | `shelf_date_type` TINYINT DEFAULT 0 | 保质期录入方式 |
| `o_goods` | `shelf_date` DATE | 生产日期/到期日 |
| `o_shop` | `opening_hours` VARCHAR(100) | 营业时间 |
| `o_shop` | `print_config` JSON | 打印机配置 |
| `o_shop` | `pay_config` JSON | 支付参数 |

### 2.3 第二期扩展表（即时零售）

| 现有表 | 扩展内容 | 用途 |
|---|---|---|
| `o_shop_platform` | 新增4条记录（1100/1200/1300/1400） | 即时零售平台配置 |
| `o_order` | 新增 delivery_type/store_id 字段 | 配送方式/履约门店 |
| `o_order_stocking` | 新增 is_delivery 字段 | 即时零售拣货 |

---

## 三、产品定位与版本划分

### 3.1 核心定位

本产品基于 **Spring Boot 4.1 + Vue 3 + MySQL 8 + Redis 7** 架构，**不是从零搭建**，而是对现有 `qihang-retail` 代码库的零售化扩展。

**与现有电商ERP的关系**：
- 现有 OMS（淘宝/京东/拼多多/抖店/微信/快手/小红书）保持不变
- **即时零售**（美团闪购/淘宝闪购/京东到家/抖音小时达）作为新平台接入，复用现有 `o_shop`/`o_order` 表
- **线下POS** 复用现有商品/库存/采购/供应商/仓储，新增收银/会员/营销层

### 3.2 用户画像

**开源版用户**
- 单店经营 / 3-5家小店的连锁（超市、便利、生鲜、母婴、烟酒、3C、文具）
- 对数据自有、零年费有强诉求
- 业主自己或1-2名店员操作

**商业版用户**
- 5-500家门店的连锁品牌
- 直营+加盟混合业态
- 有总部IT/运营团队
- 需要与ERP/财务系统对接

### 3.3 版本功能矩阵

| 功能模块 | 开源版 | 商业版 |
|---|:---:|:---:|
| 商品/库存/采购/供应商（复用现有） | ✓ | ✓ |
| POS收银（含离线） | ✓ | ✓ |
| 会员/储值/积分 | ✓ | ✓ |
| 营销促销/优惠券 | ✓ | ✓ |
| 财务对账/数据报表 | ✓ | ✓ |
| 即时零售对接（4平台） | ✓ | ✓ |
| 多门店连锁（`merchant_id`/`shop_id`天然支持） | ✓ | ✓ |
| 加盟体系/分账 | ✗ | ✓ |
| 中心仓/智能调拨（扩展现有仓库体系） | ✗ | ✓ |
| 业财一体化（凭证） | ✗ | ✓ |
| 跨店会员全域通用（扩展现有会员体系） | ✗ | ✓ |
| 私域商城对接 | ✗ | ✓ |
| AI智能补货/寻源 | ✗ | ✓ |
| 开放API/多租户SaaS | ✗ | ✓ |
| SLA与专属支持 | ✗ | ✓ |

---

## 四、系统总体设计

### 4.1 技术栈

| 层 | 技术 | 说明 |
|---|---|---|
| 后端 | Spring Boot 4.1 + MyBatis-Plus + Java 17 | 复用现有架构 |
| 数据库 | MySQL 8 + Redis 7 | 复用现有 |
| 安全 | Spring Security 7 + JWT | 复用现有 |
| 后台Web | Vue 3.5 + TypeScript + Vite 8 + Element Plus + Pinia | 复用现有 |
| **POS桌面端** | **Electron + Vue 3** | 新增，支持离线+硬件外设 |
| 移动端（可选） | uni-app | 老板/店长查看报表、远程管理 |
| 即时零售SDK | Java独立模块 | 平台API封装，定时任务拉单 |

### 4.2 双客户端形态

```
┌──────────────────────────────────────────────────────────┐
│  客户端层                                                │
│  ┌──────────────────┐  ┌───────────────────────────┐    │
│  │ Web后台管理端    │  │ Electron POS收银端        │    │
│  │ (Vue3 + 浏览器)  │  │ (Vue3 + Electron + SQLite)│    │
│  │                  │  │                           │    │
│  │ · 商品/库存管理  │  │ · 收银台（含离线模式）   │    │
│  │ · 会员/营销管理  │  │ · 班次交班                │    │
│  │ · 采购/供应商    │  │ · 小票打印（本地）        │    │
│  │ · 财务/报表      │  │ · 硬件外设（秤/钱箱/扫枪）│    │
│  │ · 即时零售配置   │  │ · 退货退款                │    │
│  │ · 系统管理       │  │ · 会员快速操作            │    │
│  └────────┬─────────┘  └─────────────┬─────────────┘    │
└───────────┼──────────────────────────┼──────────────────┘
            │  HTTPS + JWT             │
┌───────────▼──────────────────────────▼──────────────────┐
│  后端API层 (Spring Boot)                                 │
│  ┌────────┬────────┬────────┬────────┬────────┬──────┐ │
│  │  sys   │ goods  │  pos   │ member │ market │finance│ │
│  ├────────┼────────┼────────┼────────┼────────┼──────┤ │
│  │inventory│purchase│ store  │ report │  oms   │channel│ │
│  └────────┴────────┴────────┴────────┴────────┴──────┘ │
└────────────────────────┬─────────────────────────────────┘
                         │
┌────────────────────────▼─────────────────────────────────┐
│  数据层: MySQL 8 + Redis 7                                │
└──────────────────────────────────────────────────────────┘
```

### 4.3 后端模块划分

基于现有 `cn.qihangerp` 包结构，**新增 `pos/` 业务包**，不动现有电商ERP代码。

```
erp-api/src/main/java/cn/qihangerp/
├── erp/                    (现有：电商ERP后台，保持不变)
├── sys/                    (现有：系统管理，保持不变)
└── pos/                    (新增：零售POS业务)
    ├── controller/
    │   ├── cashier/        POS收银（CashierController/ShiftController/RefundController）
    │   ├── member/         会员（MemberController）
    │   ├── marketing/      营销（PromotionController）
    │   ├── store/          门店组织（StoreController/EmployeeController）
    │   ├── finance/        财务（ReconcileController）
    │   ├── report/         报表（ReportController）
    │   └── hardware/       硬件代理（PrinterController/ScaleController）
    └── config/
```

---

## 五、第一期：线下零售场景功能需求

> **复用策略**：最大化复用现有表，仅扩展少量字段，**零新增表**。

### 5.1 POS收银模块

#### 5.1.1 收银台（F-CASHIER-001）

**复用**：
- `erp_sales_order` + `erp_sales_order_item` → POS销售单
- `o_goods` + `o_goods_sku` → 商品查询
- `o_marketing_discount_rule` → 折扣规则
- `oms_shop_member` → 会员识别
- `erp_sales_person` → 营业员/收银员

**功能点**：
- 商品添加：扫码枪扫码（HID模式，零开发）、键盘输入条码（调用 `o_goods.bar_code` 查询）、按名称模糊搜索
- 数量调整：±按钮、键盘输入、计件/称重（复用 `o_goods.is_weighted`）
- 整单操作：挂单、取单、清空、锁定
- 折扣应用：调用 `o_marketing_discount_rule` 规则引擎，收银时自动计算最优折扣
- 改价：记录改价日志（复用 `sys_oper_log`）
- 会员应用：扫码/手机号识别，调用 `oms_shop_member` 接口
- 支付：现金/微信/支付宝/银行卡/会员余额/组合支付

**业务规则**：
- 单笔订单最大商品行数：200
- 挂单超过24小时自动作废
- 改价超过原价30%需店长授权
- 抹零精度支持1元/0.1元/0.01元配置

#### 5.1.2 离线收银（F-CASHIER-002）

**实现方式**：Electron端本地SQLite缓存
- 商品基础信息（最近30天，从后端API拉取）
- 会员基础信息（最近30天）
- 网络检测：心跳3秒，断网自动切换本地模式
- 离线时仅支持现金和会员余额支付
- 网络恢复后批量上传订单，服务端校验库存

#### 5.1.3 退货退款（F-CASHIER-003）

**复用**：
- `erp_sales_order` 状态扩展
- `o_refund` / `o_refund_after_sale` → 退款记录

**功能点**：
- 整单退货/部分退货
- 退款方式：原路退回、现金退款
- 退货原因（可配置字典）
- 金额>阈值需店长审核
- 退货后自动增加库存（复用 `erp_stock_in`）

#### 5.1.4 班次管理（F-CASHIER-004）

**复用**：
- `erp_sales_order` → 按营业员/时段聚合销售数据
- `erp_sales_person` → 班次绑定营业员

**功能点**：
- 开班：收银员登录后开班
- 交班：统计当班期间所有交易（按支付方式汇总）
- 班次对账单：实收 vs 应收，差异预警
- 班次历史查询（Web后台）

#### 5.1.5 小票打印（F-CASHIER-005）

**复用**：
- `o_shop` → 门店信息（打印在小票上）

**功能点**：
- 打印机支持：USB小票机（ESC/POS）、网口小票机、云小票机（飞鹅/优声云）
- 小票内容：门店名/商品列表/优惠明细/支付明细/会员积分
- 58mm/80mm纸张规格
- 打印失败自动重试2次

#### 5.1.6 称重商品（F-CASHIER-006）

**复用**：`o_goods.is_weighted` + `o_goods.unit_name`

**功能点**：
- 电子秤对接：RS232串口、USB虚拟串口
- 称重商品扫码后自动读取秤重
- 手动输入重量（秤故障时备用）

### 5.2 商品管理模块

#### 5.2.1 商品档案（F-GOODS-001）

**复用**：`o_goods`[R] + `o_goods_sku`[R]

**功能点**：
- SPU/SKU模型（现有）
- 必填字段：商品名称、分类、单位、零售价、条码
- 批量Excel导入/导出（复用现有功能）
- 批量上下架（复用 `o_goods.status`）

#### 5.2.2 商品分类（F-GOODS-002）

**复用**：`o_goods_category`[R]（多级分类/merchant_id已存在）

#### 5.2.3 品牌管理（F-GOODS-003）

**复用**：`o_goods_brand`[R]（直接可用）

### 5.3 库存管理模块

#### 5.3.1 实时库存查询（F-STOCK-001）

**复用**：`o_goods_inventory`[R] + `o_goods_inventory_batch`[R]

#### 5.3.2 入库管理（F-STOCK-002）

**复用**：`erp_stock_in`/`erp_stock_in_item`[R] + `erp_purchase_order`[R]

#### 5.3.3 出库管理（F-STOCK-003）

**复用**：`erp_stock_out`/`erp_stock_out_item`[R]

#### 5.3.4 库存盘点（F-STOCK-004）

**复用**：`erp_warehouse_stock_take`/`erp_warehouse_stock_take_item`[R]

### 5.4 采购与供应商模块

#### 5.4.1 供应商管理（F-PUR-001）

**复用**：`erp_supplier`[R]（直接可用）

#### 5.4.2 采购订单（F-PUR-002）

**复用**：`erp_purchase_order`/`erp_purchase_order_item`[R]

#### 5.4.3 采购入库（F-PUR-003）

**复用**：`erp_stock_in`[R]（关联采购订单）

### 5.5 会员管理模块

#### 5.5.1 会员档案（F-MEMBER-001）

**复用**：`oms_shop_member`[R]

**字段映射**：
| oms_shop_member 字段 | 会员功能用途 |
|---|---|
| `id` | 会员ID |
| `merchant_id` | 商户ID |
| `shop_id` | 注册门店ID |
| `phone` | 手机号（唯一标识） |
| `name` | 姓名 |
| `province/city/county/address` | 地址 |
| `status` | 状态（0未确认/1已确认） |
| `create_on` | 注册时间 |

**扩展字段（ALTER TABLE）**：
```sql
ALTER TABLE `oms_shop_member`
  ADD COLUMN `gender` TINYINT(1) DEFAULT 0 COMMENT '性别：0未知/1男/2女' AFTER `name`,
  ADD COLUMN `birthday` DATE COMMENT '生日' AFTER `gender`,
  ADD COLUMN `level_id` BIGINT DEFAULT 0 COMMENT '会员等级ID' AFTER `birthday`,
  ADD COLUMN `level_name` VARCHAR(30) DEFAULT '' COMMENT '等级名称' AFTER `level_id`,
  ADD COLUMN `total_consumption` DECIMAL(12,2) DEFAULT 0 COMMENT '累计消费' AFTER `level_name`,
  ADD COLUMN `visit_count` INT DEFAULT 0 COMMENT '消费次数' AFTER `total_consumption`,
  ADD COLUMN `points` INT DEFAULT 0 COMMENT '当前积分' AFTER `visit_count`,
  ADD COLUMN `stored_balance` DECIMAL(12,2) DEFAULT 0 COMMENT '储值余额' AFTER `points`,
  ADD COLUMN `last_visit_time` DATETIME COMMENT '最后消费时间' AFTER `stored_balance`;
```

#### 5.5.2 会员等级（F-MEMBER-002）

**方案A（推荐）**：复用 `o_marketing_discount_rule` 的会员折扣能力

**方案B**：在 `oms_shop_member.level_id` 基础上，用字典表 `sys_dict_data` 存储等级配置

**功能点**：
- 等级定义：等级名称、升级条件（累计消费/累计积分）、等级权益（折扣率/积分倍率）
- 自动升级：消费后触发规则引擎

#### 5.5.3 储值管理（F-MEMBER-003）

**复用**：`oms_shop_member.stored_balance`（扩展字段）

**功能点**：
- 充值方案：充1000送100、充2000送300等
- 储值消费：会员余额支付自动扣减
- 储值流水：记录在 `sys_oper_log`（操作日志）

#### 5.5.4 积分管理（F-MEMBER-004）

**复用**：`oms_shop_member.points`（扩展字段）

**功能点**：
- 积分获取：消费按金额积分（1元=1积分，可配置）
- 积分使用：积分抵扣现金
- 积分流水：记录在 `sys_oper_log`

### 5.6 营销促销模块

#### 5.6.1 促销活动（F-MKT-001）

**复用**：`o_marketing_discount_rule`[R]

**现有字段映射**：
| 字段 | 用途 |
|---|---|
| `rule_name` | 规则名称 |
| `discount_type` | 折扣类型：1百分比/2固定金额 |
| `discount_value` | 折扣值 |
| `apply_scope` | 适用范围：1全部/2商户/3门店 |
| `apply_merchant_id` | 适用商户 |
| `apply_shop_id` | 适用门店 |
| `min_order_amount` | 最低消费金额 |
| `start_time` / `end_time` | 生效时段 |
| `priority` | 优先级 |
| `total_quota` / `used_quota` | 使用次数限制 |

**扩展 `discount_type` 值**：
- 1 = 百分比折扣（现有）
- 2 = 固定金额折扣（现有）
- 3 = 满减（新增）
- 4 = 第二件半价（新增）
- 5 = 限时秒杀（新增）

### 5.7 门店与组织模块

#### 5.7.1 门店档案（F-STORE-001）

**复用**：`o_shop`[R]（type=999 表示线下门店）

**现有字段映射**：
| o_shop 字段 | 门店功能用途 |
|---|---|
| `id` | 门店ID |
| `name` | 门店名称 |
| `type` | 999=线下门店 |
| `merchant_id` | 商户ID |
| `province/city/district/address` | 门店地址 |
| `contact` | 联系人 |
| `phone` | 联系电话 |
| `status` | 状态（1正常/2已删除） |

**扩展字段（ALTER TABLE）**：
```sql
ALTER TABLE `o_shop`
  ADD COLUMN `number` VARCHAR(50) COMMENT '门店编码' AFTER `name`,
  ADD COLUMN `opening_hours` VARCHAR(100) COMMENT '营业时间，如：08:00-22:00' AFTER `address`,
  ADD COLUMN `register_count` INT DEFAULT 1 COMMENT '收银台数量' AFTER `opening_hours`,
  ADD COLUMN `print_config` JSON COMMENT '打印机配置' AFTER `register_count`,
  ADD COLUMN `pay_config` JSON COMMENT '支付参数' AFTER `print_config`;
```

#### 5.7.2 营业员管理（F-STORE-002）

**复用**：`erp_sales_person`[R]

**现有字段映射**：
| erp_sales_person 字段 | 营业员功能用途 |
|---|---|
| `id` | 员工ID |
| `name` | 姓名 |
| `mobile` | 手机号 |
| `employee_no` | 工号 |
| `merchant_id` | 商户ID |
| `shop_id` | 所属门店 |
| `commission_rate` | 提成比例 |
| `status` | 状态（0禁用/1启用） |

**扩展字段（ALTER TABLE）**：
```sql
ALTER TABLE `erp_sales_person`
  ADD COLUMN `position` VARCHAR(50) DEFAULT '' COMMENT '岗位：店长/收银员/理货员' AFTER `employee_no`,
  ADD COLUMN `position_code` VARCHAR(20) DEFAULT '' COMMENT '岗位编码' AFTER `position`;
```

#### 5.7.3 提成管理（F-STORE-003）

**复用**：`erp_sales_person.commission_rate`（提成比例）

**功能点**：
- 提成规则：基于 `commission_rate` 字段
- 提成报表：按员工/按门店/按时段

### 5.8 财务对账模块

#### 5.8.1 收款对账（F-FIN-001）

**复用**：`fms_finance_ledger`[R] + `erp_sales_order`[R]

**功能点**：
- 按支付方式对账
- 按日对账
- 差异处理：标记差异原因

### 5.9 数据报表模块

**复用**：`o_shop_daily`[R] + `o_shop_daily_detail`[R]

| 报表 | 数据源 |
|---|---|
| 销售日报/月报 | `erp_sales_order` + `o_shop_daily` |
| 商品销售排行 | `erp_sales_order_item` |
| 库存报表 | `o_goods_inventory` + `o_goods_inventory_batch` |
| 员工业绩 | `erp_sales_order` + `erp_sales_person` |

### 5.10 系统管理模块（复用现有）

复用 `sys/` 现有功能：用户、角色、菜单、字典、参数配置、操作日志、登录日志、部门。

---

## 六、第二期：即时零售对接功能需求

> 复用现有 OMS 架构，**仅扩展少量字段**。

### 6.1 平台店铺授权

**复用**：`o_shop` + `o_shop_platform`

**扩展**：在 `o_shop_platform` 中新增4条记录
- 1100 美团闪购
- 1200 淘宝闪购（饿了么零售）
- 1300 京东到家
- 1400 抖音小时达

### 6.2 商品同步

**复用**：`o_goods` + `o_goods_sku`

**新增字段**：
```sql
ALTER TABLE `o_goods`
  ADD COLUMN `platform_goods_id` VARCHAR(100) COMMENT '平台商品ID' AFTER `bar_code`,
  ADD COLUMN `platform_sku_id` VARCHAR(100) COMMENT '平台SKU ID' AFTER `platform_goods_id`;
```

### 6.3 订单管理

**复用**：`o_order` + `o_order_item`

**扩展字段**：
```sql
ALTER TABLE `o_order`
  ADD COLUMN `delivery_type` TINYINT DEFAULT 0 COMMENT '配送方式：0平台/1自配/2自提' AFTER `order_status`,
  ADD COLUMN `store_id` BIGINT DEFAULT 0 COMMENT '履约门店ID' AFTER `delivery_type`;
```

---

## 七、商业版进阶功能

### 7.1 加盟体系（F-ENT-001）
- 直营/加盟门店区分（复用 `o_shop.type`）
- 加盟结算（含三级加盟商体系）

### 7.2 中心仓与智能调拨（F-ENT-002）
- 中心仓+门店仓两级库存（复用 `erp_warehouse`）

### 7.3 业财一体化（F-ENT-003）
- 业务单据自动生成凭证
- 对接金蝶/用友财务系统

### 7.4 跨店会员全域通用（F-ENT-004）
- 会员资产全渠道统一

### 7.5 私域商城对接（F-ENT-005）
- 有赞/微盟/微店等对接

### 7.6 AI能力（F-ENT-006）
- 智能补货预测
- 智能寻源

### 7.7 开放API与多租户SaaS（F-ENT-007）
- 2000+开放API
- 多租户SaaS架构

---

## 八、非功能性需求

### 8.1 性能
- POS收银响应：扫码到显示商品 < 200ms
- 收银提交到小票打印 < 1秒
- 后端API平均响应 < 500ms

### 8.2 可用性
- 离线收银不中断业务
- 服务端99.5%可用性
- 数据每日自动备份

### 8.3 安全
- JWT Token认证（复用 `security` 模块）
- 按钮级权限控制（复用 `sys_role`/`sys_menu`）
- 敏感操作日志（改价/退款/盘点调整）

### 8.4 兼容性
- POS桌面端：Windows 10+/macOS 10.15+
- Web后台：Chrome 90+/Edge 90+/Safari 14+
- 数据库：MySQL 8.0+
- Redis：7.0+

---

## 九、实施计划与里程碑

### 9.1 第一期：线下POS全场景（零新增表）

**MVP版本（4周）**
- 扩展 `o_goods` 字段（is_weighted/shelf_date_type/shelf_date）
- 扩展 `o_shop` 字段（number/opening_hours/register_count/print_config/pay_config）
- 扩展 `erp_sales_person` 字段（position/position_code）
- 扩展 `oms_shop_member` 字段（gender/birthday/level_id/total_consumption/visit_count/points/stored_balance/last_visit_time）
- POS收银台（复用 erp_sales_order）
- 门店/营业员管理（复用 o_shop/erp_sales_person）
- Electron POS端基础框架

**Beta版本（再4周）**
- 会员档案 + 储值 + 积分（复用 oms_shop_member）
- 促销折扣（复用 o_marketing_discount_rule）
- 采购入库 + 供应商（复用现有）
- 基础报表（复用 o_shop_daily）
- 微信/支付宝聚合支付

**Release版本（再4周）**
- 库存盘点/调拨（复用现有）
- 退货退款（复用 o_refund）
- 离线收银（SQLite缓存）
- 电子秤/钱箱对接
- Electron桌面端打包发布

### 9.2 第二期：即时零售对接

**美团闪购对接（6-8周）**
- 平台授权（扩展 o_shop_platform）
- 商品同步（扩展 o_goods）
- 订单拉取与处理（复用 o_order）

**其他平台（4-6周/个）**
- 淘宝闪购、京东到家、抖音小时达

---

## 十、风险与依赖

### 10.1 技术风险

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| Electron离线模式复杂度高 | 中 | 先实现"在线+本地缓存"模式 |
| 即时零售平台API变更 | 高 | SDK抽象层隔离 |

### 10.2 业务风险

| 风险 | 影响 | 缓解措施 |
|---|---|---|
| 开源版功能过多导致维护成本高 | 中 | 模块化，社区共建 |
| 商业版与开源版边界不清 | 高 | 明确功能矩阵 |

### 10.3 外部依赖

- 微信支付/支付宝支付商户号（商家自行申请）
- 即时零售平台开发者账号（商家自行申请）

---

## 十一、扩展表汇总

### 第一期仅需扩展字段（0新增表）

| 序号 | 表名 | 新增字段 | 说明 |
|---|---|---|---|
| 1 | `o_goods` | is_weighted, shelf_date_type, shelf_date | 称重/保质期 |
| 2 | `o_shop` | number, opening_hours, register_count, print_config, pay_config | 门店扩展 |
| 3 | `erp_sales_person` | position, position_code | 岗位信息 |
| 4 | `oms_shop_member` | gender, birthday, level_id, total_consumption, visit_count, points, stored_balance, last_visit_time | 会员扩展 |

### 第二期仅需扩展字段（0新增表）

| 序号 | 表名 | 新增字段 | 说明 |
|---|---|---|---|
| 5 | `o_goods` | platform_goods_id, platform_sku_id | 平台商品映射 |
| 6 | `o_order` | delivery_type, store_id | 配送/门店 |

---

## 十二、附录

### 12.1 术语表

| 术语 | 说明 |
|---|---|
| POS | Point of Sale，销售点收银系统 |
| SPU | Standard Product Unit，标准化产品单元 |
| SKU | Stock Keeping Unit，库存量单位 |
| OMS | Order Management System，订单管理系统 |
| 即时零售 | 线上下单+门店发货+小时达的零售模式 |
| merchant_id | 商户ID，支持多租户或多店铺连锁 |
| shop_id | 店铺ID，对应o_shop，type=999为线下门店 |

### 12.2 参考资料

- [万里牛零售](https://www.hupun.com/product/pos/index.html)
- [美团闪购开放平台](https://tscc.meituan.com/)
- [牵牛花即时零售](https://www.qianniuhua.net/)
- [金蝶AI星辰零售云](https://m.kingdee.com/product/stellar_retail.html)

### 12.3 修订记录

| 版本 | 日期 | 修订内容 | 作者 |
|---|---|---|---|
| v1.0 | 2026-07-22 | 初始版本 | - |
| v2.0 | 2026-07-22 | 基于现有SQL表结构重新设计 | - |
| v2.1 | 2026-07-22 | 修复文档结构、优化SQL DDL | - |
| v3.0 | 2026-07-22 | **零新增表方案**，最大化复用现有161张表 | - |
