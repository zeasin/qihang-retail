-- 为 o_order 表添加 order_source 字段
-- 字段说明：与 oms_shop_order.order_source 保持一致
-- 用于标识订单来源渠道（POS、美团闪购、京东到家等）

ALTER TABLE `o_order` 
ADD COLUMN `order_source` varchar(12) DEFAULT NULL COMMENT '订单来源：POS=POS收银、MT_FLASH=美团闪购、MT_WM=美团外卖、JD_DJ=京东到家、TAO_FLASH=淘宝闪购、EL_Flash=饿了么、WECHAT_MINI=微信小程序' 
AFTER `order_num`;
