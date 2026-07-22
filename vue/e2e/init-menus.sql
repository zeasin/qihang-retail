-- ============================================================
-- 启航零售 ERP 菜单数据初始化脚本
-- 适用数据库: MySQL 8.0+
-- ============================================================

-- 1. 系统管理（parent_id = 0, menu_id = 1）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (1, '系统管理', 0, 100, 'system', NULL, NULL, '1', '0', 'M', '0', '0', '', 'system', 'admin', NOW(), NULL, NULL, '系统管理目录');

INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (2, '用户管理', 1, 1, 'user', 'system/user/index', NULL, '1', '0', 'C', '0', '0', 'system:user:list', 'user', 'admin', NOW(), NULL, NULL, '用户管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (3, '角色管理', 1, 2, 'role', 'system/role/index', NULL, '1', '0', 'C', '0', '0', 'system:role:list', 'peoples', 'admin', NOW(), NULL, NULL, '角色管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (4, '菜单管理', 1, 3, 'menu', 'system/menu/index', NULL, '1', '0', 'C', '0', '0', 'system:menu:list', 'tree-table', 'admin', NOW(), NULL, NULL, '菜单管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (5, '字典管理', 1, 4, 'dict', 'system/dict/index', NULL, '1', '0', 'C', '0', '0', 'system:dict:list', 'dict', 'admin', NOW(), NULL, NULL, '字典管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (6, '系统配置', 1, 5, 'config', 'system/config/index', NULL, '1', '0', 'C', '0', '0', 'system:config:list', 'edit', 'admin', NOW(), NULL, NULL, '系统配置菜单');

-- 2. 采购管理（parent_id = 0, menu_id = 10）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (10, '采购管理', 0, 10, 'purchase', NULL, NULL, '1', '0', 'M', '0', '0', '', 'shopping', 'admin', NOW(), NULL, NULL, '采购管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (11, '采购订单', 10, 1, 'order', 'purchase/order/index', NULL, '1', '0', 'C', '0', '0', 'purchase:order:list', 'list', 'admin', NOW(), NULL, NULL, '采购订单菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (12, '采购入库', 10, 2, 'ship', 'purchase/ship/index', NULL, '1', '0', 'C', '0', '0', 'purchase:ship:list', 'inbox', 'admin', NOW(), NULL, NULL, '采购入库菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (13, '承运商管理', 10, 3, 'shipper', 'purchase/shipper/index', NULL, '1', '0', 'C', '0', '0', 'purchase:shipper:list', 'truck', 'admin', NOW(), NULL, NULL, '承运商管理菜单');

-- 3. 订单管理（parent_id = 0, menu_id = 20）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (20, '订单管理', 0, 20, 'order', NULL, NULL, '1', '0', 'M', '0', '0', '', 'order', 'admin', NOW(), NULL, NULL, '订单管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (21, '订单列表', 20, 1, 'order_list', 'order/order_list', NULL, '1', '0', 'C', '0', '0', 'order:list', 'list', 'admin', NOW(), NULL, NULL, '订单列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (22, '订单明细', 20, 2, 'item_list', 'order/item_list', NULL, '1', '0', 'C', '0', '0', 'order:item:list', 'table', 'admin', NOW(), NULL, NULL, '订单明细菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (23, '店铺订单', 20, 3, 'shopOrder', 'order/shopOrder/index', NULL, '1', '0', 'C', '0', '0', 'order:shopOrder:list', 'shop', 'admin', NOW(), NULL, NULL, '店铺订单菜单');

-- 4. 售后管理（parent_id = 0, menu_id = 30）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (30, '售后管理', 0, 30, 'afterSale', NULL, NULL, '1', '0', 'M', '0', '0', '', 'after-sales', 'admin', NOW(), NULL, NULL, '售后管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (31, '售后列表', 30, 1, 'index', 'afterSale/index', NULL, '1', '0', 'C', '0', '0', 'afterSale:list', 'list', 'admin', NOW(), NULL, NULL, '售后列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (32, '退款管理', 30, 2, 'refund', 'refund/index', NULL, '1', '0', 'C', '0', '0', 'refund:list', 'money', 'admin', NOW(), NULL, NULL, '退款管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (33, '店铺退款', 30, 3, 'shopRefund', 'refund/shopRefund/index', NULL, '1', '0', 'C', '0', '0', 'refund:shopRefund:list', 'shop', 'admin', NOW(), NULL, NULL, '店铺退款菜单');

-- 5. 履约发货（parent_id = 0, menu_id = 40）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (40, '履约发货', 0, 40, 'shipping', NULL, NULL, '1', '0', 'M', '0', '0', '', 'shipping', 'admin', NOW(), NULL, NULL, '履约发货目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (41, '发货管理', 40, 1, 'index', 'shipping/index', NULL, '1', '0', 'C', '0', '0', 'shipping:list', 'list', 'admin', NOW(), NULL, NULL, '发货管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (42, '手动发货', 40, 2, 'manual_ship', 'shipping/manual_ship/order_list', NULL, '1', '0', 'C', '0', '0', 'shipping:manualShip:list', 'hand', 'admin', NOW(), NULL, NULL, '手动发货菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (43, '发货记录', 40, 3, 'record', 'shipping/record/index', NULL, '1', '0', 'C', '0', '0', 'shipping:record:list', 'documentation', 'admin', NOW(), NULL, NULL, '发货记录菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (44, '物流管理', 40, 4, 'logistics', 'shipping/logistics/index', NULL, '1', '0', 'C', '0', '0', 'shipping:logistics:list', 'logistics', 'admin', NOW(), NULL, NULL, '物流管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (45, '配货管理', 40, 5, 'stocking', 'shipping/stocking/index', NULL, '1', '0', 'C', '0', '0', 'shipping:stocking:list', 'list', 'admin', NOW(), NULL, NULL, '配货管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (46, '电子面单', 40, 6, 'ewaybillPrint', 'shipping/ewaybillPrint/index', NULL, '1', '0', 'C', '0', '0', 'shipping:ewaybillPrint:list', 'printer', 'admin', NOW(), NULL, NULL, '电子面单菜单');

-- 6. 仓库管理（parent_id = 0, menu_id = 50）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (50, '仓库管理', 0, 50, 'wms', NULL, NULL, '1', '0', 'M', '0', '0', '', 'warehouse', 'admin', NOW(), NULL, NULL, '仓库管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (51, '仓库列表', 50, 1, 'warehouse', 'wms/warehouse/index', NULL, '1', '0', 'C', '0', '0', 'wms:warehouse:list', 'list', 'admin', NOW(), NULL, NULL, '仓库列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (52, 'WMS商品', 50, 2, 'goods', 'wms/goods/index', NULL, '1', '0', 'C', '0', '0', 'wms:goods:list', 'goods', 'admin', NOW(), NULL, NULL, 'WMS商品菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (53, '入库管理', 50, 3, 'stockIn', 'wms/stockIn/index', NULL, '1', '0', 'C', '0', '0', 'wms:stockIn:list', 'inbox', 'admin', NOW(), NULL, NULL, '入库管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (54, '出库管理', 50, 4, 'stockOut', 'wms/stockOut/index', NULL, '1', '0', 'C', '0', '0', 'wms:stockOut:list', 'outbox', 'admin', NOW(), NULL, NULL, '出库管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (55, '库存预警', 50, 5, 'stock_alert', 'wms/stock_alert/index', NULL, '1', '0', 'C', '0', '0', 'wms:stockAlert:list', 'alert', 'admin', NOW(), NULL, NULL, '库存预警菜单');

-- 7. 商品管理（parent_id = 0, menu_id = 60）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (60, '商品管理', 0, 60, 'goods', NULL, NULL, '1', '0', 'M', '0', '0', '', 'goods', 'admin', NOW(), NULL, NULL, '商品管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (61, '商品列表', 60, 1, 'goods_list', 'goods/goods_list', NULL, '1', '0', 'C', '0', '0', 'goods:list', 'list', 'admin', NOW(), NULL, NULL, '商品列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (62, '规格管理', 60, 2, 'spec', 'goods/spec/index', NULL, '1', '0', 'C', '0', '0', 'goods:spec:list', 'spec', 'admin', NOW(), NULL, NULL, '规格管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (63, '分类管理', 60, 3, 'category', 'goods/category/index', NULL, '1', '0', 'C', '0', '0', 'goods:category:list', 'category', 'admin', NOW(), NULL, NULL, '分类管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (64, '品牌管理', 60, 4, 'brand', 'goods/brand/index', NULL, '1', '0', 'C', '0', '0', 'goods:brand:list', 'brand', 'admin', NOW(), NULL, NULL, '品牌管理菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (65, '供应商管理', 60, 5, 'supplier', 'goods/supplier/index', NULL, '1', '0', 'C', '0', '0', 'goods:supplier:list', 'supplier', 'admin', NOW(), NULL, NULL, '供应商管理菜单');

-- 8. 店铺管理（parent_id = 0, menu_id = 70）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (70, '店铺管理', 0, 70, 'shop', NULL, NULL, '1', '0', 'M', '0', '0', '', 'shop', 'admin', NOW(), NULL, NULL, '店铺管理目录');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (71, '店铺列表', 70, 1, 'index', 'shop/index', NULL, '1', '0', 'C', '0', '0', 'shop:list', 'list', 'admin', NOW(), NULL, NULL, '店铺列表菜单');
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (72, '商户管理', 70, 2, 'merchant', 'shop/merchant/index', NULL, '1', '0', 'C', '0', '0', 'shop:merchant:list', 'merchant', 'admin', NOW(), NULL, NULL, '商户管理菜单');

-- 9. 首页 Dashboard（parent_id = 0, menu_id = 80）
INSERT INTO `sys_menu` (`menu_id`, `menu_name`, `parent_id`, `order_num`, `path`, `component`, `query`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (80, '首页', 0, 1, 'index', 'dashboard/index', NULL, '1', '0', 'C', '0', '0', 'dashboard:list', 'dashboard', 'admin', NOW(), NULL, NULL, '首页菜单');

-- ============================================================
-- 创建 admin 角色
-- ============================================================
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_key`, `role_sort`, `data_scope`, `menu_check_strictly`, `dept_check_strictly`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`)
VALUES (1, '超级管理员', 'admin', 1, '1', '1', '1', '0', '0', 'admin', NOW(), NULL, NULL, '超级管理员');

-- ============================================================
-- 关联 admin 角色到所有菜单
-- ============================================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1),  (1, 2),  (1, 3),  (1, 4),  (1, 5),  (1, 6),
(1, 10), (1, 11), (1, 12), (1, 13),
(1, 20), (1, 21), (1, 22), (1, 23),
(1, 30), (1, 31), (1, 32), (1, 33),
(1, 40), (1, 41), (1, 42), (1, 43), (1, 44), (1, 45), (1, 46),
(1, 50), (1, 51), (1, 52), (1, 53), (1, 54), (1, 55),
(1, 60), (1, 61), (1, 62), (1, 63), (1, 64), (1, 65),
(1, 70), (1, 71), (1, 72),
(1, 80);

-- ============================================================
-- 关联 admin 用户到 admin 角色
-- ============================================================
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);
