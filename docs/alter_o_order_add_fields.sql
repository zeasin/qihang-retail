-- 添加订单来源字段
ALTER TABLE o_order ADD COLUMN `order_source` varchar(50) DEFAULT NULL COMMENT '订单来源：POS=POS收银、MT_FLASH=美团闪购、MT_WM=美团外卖、JD_DJ=京东到家' AFTER `order_num`;

-- 添加配送方式字段
ALTER TABLE o_order ADD COLUMN `delivery_method` int DEFAULT NULL COMMENT '配送方式：1=现结，2=到店自提，3=商家配送，4=骑手配送' AFTER `town`;
