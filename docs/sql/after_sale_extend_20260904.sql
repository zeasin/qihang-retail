-- ============================================================
-- 启航零售POS系统 - 售后台账扩展脚本
-- 版本: v1.0 (零新增表方案)
-- 日期: 2026-09-04
-- 说明: 扩展 o_refund 表字段 + sys_config 售后配置
-- ============================================================

-- ============================================================
-- 1. o_refund 表扩展（审核、退款执行、退货收货记录）
-- 说明: 现有SQL表已有exchange/send_logistics等字段，此处补审核相关
-- ============================================================
ALTER TABLE `o_refund`
  ADD COLUMN `audit_by` VARCHAR(64) DEFAULT NULL COMMENT '审核人' AFTER `erp_status`,
  ADD COLUMN `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间' AFTER `audit_by`,
  ADD COLUMN `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注' AFTER `audit_time`,
  ADD COLUMN `refund_method` VARCHAR(20) DEFAULT NULL COMMENT '退款方式：cash现金/original原路退回/balance退到余额' AFTER `audit_remark`,
  ADD COLUMN `refund_time` DATETIME DEFAULT NULL COMMENT '退款执行时间' AFTER `refund_method`,
  ADD COLUMN `refund_by` VARCHAR(64) DEFAULT NULL COMMENT '退款执行人' AFTER `refund_time`,
  ADD COLUMN `receive_by` VARCHAR(64) DEFAULT NULL COMMENT '退货收货人' AFTER `refund_by`;

-- ============================================================
-- 2. sys_config 售后配置（授权期、审核阈值、退货原因）
-- ============================================================
INSERT INTO `sys_config` (`config_name`, `config_key`, `config_value`, `config_type`, `create_by`, `create_time`, `remark`) VALUES
('退货退款期限(天)', 'retail.return.period_days', '7', 'Y', 'admin', NOW(), '退货退款授权期，订单完成超过此天数不可发起退货退款'),
('换货期限(天)', 'retail.exchange.period_days', '15', 'Y', 'admin', NOW(), '换货授权期，订单完成超过此天数不可发起换货'),
('退款审核阈值(元)', 'retail.refund.audit_threshold', '200', 'Y', 'admin', NOW(), '退款金额超过此值需店长审核，0表示全部需审核'),
('退货原因列表', 'retail.return.reasons', '商品质量问题,不想要了,商品描述不符,发错货,少件漏发,其他原因', 'Y', 'admin', NOW(), '退货原因选项，逗号分隔');

-- ============================================================
-- 3. erp_status 新增简化状态码说明（复用现有字段）
-- ============================================================
-- erp_status 值定义（售后台账专用）:
-- 0  = 待审核
-- 1  = 审核通过-待退货（退货退款/换货类型）
-- 2  = 审核通过-待退款（仅退款，或退货已收货）
-- 3  = 待换发（换货类型，退货已收货，等待换货发货）
-- 10 = 已完成
-- 11 = 已取消
-- 12 = 已拒绝

-- ============================================================
-- 完成提示
-- ============================================================
-- 执行完毕后：
-- 1. o_refund 表新增 7 个字段（审核+退款执行+收货记录）
-- 2. sys_config 新增 4 条售后配置（授权期+审核阈值+退货原因）
