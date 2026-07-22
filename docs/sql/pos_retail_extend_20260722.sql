-- ============================================================
-- 启航零售POS系统 - 数据库扩展脚本
-- 版本: v3.0 (零新增表方案)
-- 日期: 2026-07-22
-- 说明: 扩展现有表字段，支持POS收银、会员、营销等功能
-- ============================================================

-- ============================================================
-- 1. 门店管理扩展 (o_shop)
-- 说明: type=999 表示线下门店
-- ============================================================
ALTER TABLE `o_shop`
  ADD COLUMN `number` VARCHAR(50) COMMENT '门店编码' AFTER `name`,
  ADD COLUMN `opening_hours` VARCHAR(100) DEFAULT '' COMMENT '营业时间，如：08:00-22:00' AFTER `address`,
  ADD COLUMN `register_count` INT DEFAULT 1 COMMENT '收银台数量' AFTER `opening_hours`,
  ADD COLUMN `print_config` JSON COMMENT '打印机配置（型号/IP/端口）' AFTER `register_count`,
  ADD COLUMN `pay_config` JSON COMMENT '支付参数（微信/支付宝商户号）' AFTER `print_config`;

-- ============================================================
-- 2. 营业员管理扩展 (erp_sales_person)
-- ============================================================
ALTER TABLE `erp_sales_person`
  ADD COLUMN `position` VARCHAR(50) DEFAULT '' COMMENT '岗位：店长/收银员/理货员' AFTER `employee_no`,
  ADD COLUMN `position_code` VARCHAR(20) DEFAULT '' COMMENT '岗位编码' AFTER `position`;

-- ============================================================
-- 3. 会员管理扩展 (oms_shop_member)
-- ============================================================
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

-- 添加索引
ALTER TABLE `oms_shop_member`
  ADD INDEX `idx_merchant` (`merchant_id`),
  ADD INDEX `idx_level` (`level_id`),
  ADD UNIQUE INDEX `uk_phone_merchant` (`phone`, `merchant_id`);

-- ============================================================
-- 4. 商品管理扩展 (o_goods)
-- 说明: 称重商品、保质期管理
-- ============================================================
ALTER TABLE `o_goods`
  ADD COLUMN `is_weighted` TINYINT(1) DEFAULT 0 COMMENT '是否称重商品' AFTER `status`,
  ADD COLUMN `shelf_date_type` TINYINT(1) DEFAULT 0 COMMENT '保质期录入方式：0无需/1生产日期+天数/2固定到期日' AFTER `is_weighted`,
  ADD COLUMN `shelf_date` DATE COMMENT '生产日期或到期日' AFTER `shelf_date_type`;

-- ============================================================
-- 5. 营销折扣规则扩展 (o_marketing_discount_rule)
-- 说明: 扩展 discount_type 支持更多促销类型
-- 现有: 1=百分比折扣, 2=固定金额折扣
-- 新增: 3=满减, 4=第二件半价, 5=限时秒杀
-- ============================================================
-- discount_type 字段已存在，只需在业务逻辑中扩展值即可
-- 无需 ALTER TABLE

-- ============================================================
-- 6. 即时零售扩展 (第二期，可选)
-- ============================================================

-- 6.1 商品平台映射字段
ALTER TABLE `o_goods`
  ADD COLUMN `platform_goods_id` VARCHAR(100) COMMENT '平台商品ID' AFTER `bar_code`,
  ADD COLUMN `platform_sku_id` VARCHAR(100) COMMENT '平台SKU ID' AFTER `platform_goods_id`;

-- 6.2 订单配送字段
ALTER TABLE `o_order`
  ADD COLUMN `delivery_type` TINYINT DEFAULT 0 COMMENT '配送方式：0平台配送/1商家自配/2到店自提' AFTER `order_status`,
  ADD COLUMN `delivery_company` VARCHAR(50) COMMENT '配送公司（美团跑腿/蜂鸟/达达）' AFTER `delivery_type`,
  ADD COLUMN `delivery_order_no` VARCHAR(100) COMMENT '配送单号' AFTER `delivery_company`,
  ADD COLUMN `store_id` BIGINT DEFAULT 0 COMMENT '履约门店ID' AFTER `delivery_order_no`,
  ADD INDEX `idx_store` (`store_id`);

-- 6.3 拣货单扩展
ALTER TABLE `o_order_stocking`
  ADD COLUMN `is_delivery` TINYINT DEFAULT 0 COMMENT '是否即时零售拣货单' AFTER `status`;

-- 6.4 门店配送范围
ALTER TABLE `o_shop`
  ADD COLUMN `delivery_range_km` DECIMAL(4,1) COMMENT '配送范围（公里）' AFTER `pay_config`,
  ADD COLUMN `delivery_address` VARCHAR(255) COMMENT '即时零售配送地址' AFTER `delivery_range_km`;

-- ============================================================
-- 完成提示
-- ============================================================
-- 执行完毕后请检查：
-- 1. o_shop 表新增 5 个字段（门店管理）
-- 2. erp_sales_person 表新增 2 个字段（营业员管理）
-- 3. oms_shop_member 表新增 9 个字段 + 3 个索引（会员管理）
-- 4. o_goods 表新增 5 个字段（商品管理）
-- 5. o_order 表新增 4 个字段（订单管理，第二期）
-- 6. o_order_stocking 表新增 1 个字段（拣货管理，第二期）
