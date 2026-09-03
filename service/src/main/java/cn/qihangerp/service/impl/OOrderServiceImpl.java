package cn.qihangerp.service.impl;

import cn.qihangerp.common.DateHelper;
import cn.qihangerp.enums.*;
import cn.qihangerp.mapper.*;
import cn.qihangerp.model.entity.ErpLogisticsCompany;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.vo.OrderDiscountVo;
import cn.qihangerp.model.vo.SalesDailyVo;
import cn.qihangerp.model.vo.WaitShipReportVo;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.request.OrderSearchRequest;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
* @author qilip
* @description 针对表【o_order(订单表)】的数据库操作Service实现
* @createDate 2024-03-09 13:15:57
*/
@Slf4j
@AllArgsConstructor
@Service
public class OOrderServiceImpl extends ServiceImpl<OOrderMapper, OOrder>
    implements OOrderService {
    private final OOrderMapper orderMapper;
    private final OOrderItemMapper orderItemMapper;
    private final OOrderStockingMapper shipOrderMapper;
    private final OOrderStockingItemMapper shipOrderItemMapper;
    private final ErpLogisticsCompanyMapper erpLogisticsCompanyMapper;
    private final OOrderStockingMapper orderStockingMapper;
    private final OOrderStockingItemMapper orderStockingItemMapper;

    @Override
    public List<OOrder> getList(OOrder order) {
        return orderMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Override
    public PageResult<OOrder> queryPageList(OrderSearchRequest bo, PageQuery pageQuery) {
        if (org.springframework.util.StringUtils.hasText(bo.getStartTime())) {
            boolean b = DateHelper.isValidDate(bo.getStartTime());
            if (!b) {
//                bo.setStartTime(bo.getStartTime() + " 00:00:00");
                bo.setStartTime("");
            }
        }
        if (org.springframework.util.StringUtils.hasText(bo.getEndTime())) {
            boolean b = DateHelper.isValidDate(bo.getEndTime());
            if (!b) {
//                bo.setEndTime(bo.getEndTime() + " 23:59:59");
                bo.setEndTime("");
            }
        }else{
            bo.setEndTime(bo.getStartTime());
        }

        LambdaQueryWrapper<OOrder> queryWrapper = new LambdaQueryWrapper<OOrder>()
                .eq(bo.getShopId() != null, OOrder::getShopId, bo.getShopId())
                .eq(bo.getMerchantId() != null, OOrder::getMerchantId, bo.getMerchantId())
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderNum()), OOrder::getOrderNum, bo.getOrderNum())
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderSource()), OOrder::getOrderSource, bo.getOrderSource())
                .eq(bo.getOrderStatus() != null, OOrder::getOrderStatus, bo.getOrderStatus())
//                .eq(bo.getRefundStatus()!=null,OOrder::getRefundStatus,bo.getRefundStatus())
                .eq(bo.getShopType() != null, OOrder::getShopType, bo.getShopType())
                .ge(org.springframework.util.StringUtils.hasText(bo.getStartTime()), OOrder::getOrderTime, bo.getStartTime()+ " 00:00:00")
                .le(org.springframework.util.StringUtils.hasText(bo.getEndTime()), OOrder::getOrderTime, bo.getEndTime()+ " 23:59:59")
                .eq(bo.getShipStatus() != null, OOrder::getShipStatus, bo.getShipStatus())
                .eq(bo.getDistStatus() != null, OOrder::getDistStatus, bo.getDistStatus())
//                .eq(bo.getErpPushStatus()!=null && bo.getErpPushStatus() == 0,OOrder::getErpPushStatus,0)
//                .eq(bo.getErpPushStatus()!=null && bo.getErpPushStatus() == 100,OOrder::getErpPushStatus,100)
//                .eq(bo.getErpPushStatus()!=null && bo.getErpPushStatus() == 200,OOrder::getErpPushStatus,200)
//                .gt(bo.getErpPushStatus()!=null && bo.getErpPushStatus() == 500,OOrder::getErpPushStatus,200)
//                .eq(org.springframework.util.StringUtils.hasText(bo.getReceiverName()),OOrder::getReceiverName,bo.getReceiverName())
//                .like(org.springframework.util.StringUtils.hasText(bo.getReceiverMobile()),OOrder::getReceiverMobile,bo.getReceiverMobile())
                ;
//        if(bo.getErpPushStatus()!=null) {
//            if (bo.getErpPushStatus() == 0) {
//                // 未推送
//                queryWrapper.eq(OOrder::getErpPushResult, 0);
//            } else if (bo.getErpPushStatus() == 200) {
//                // 推送成功
//                queryWrapper.eq(OOrder::getErpPushResult, 200);
//            } else if (bo.getErpPushStatus() == 500) {
//                // 推送失败
//                queryWrapper.gt(OOrder::getErpPushResult, 200);
//            }
//        }
        pageQuery.setOrderByColumn("order_time");
        pageQuery.setIsAsc("desc");
        Page<OOrder> pages = orderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if (pages.getRecords() != null) {
            for (OOrder order : pages.getRecords()) {
//                order.setItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, order.getId())));
                order.setItemVoList(orderItemMapper.selectOrderItemListByOrderId(Long.parseLong(order.getId())));
            }
        }

        return PageResult.build(pages);
    }

    /**
     * 待分配发货清单
     *
     * @param bo
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult<OOrder> queryWaitDistOrderPageList(OrderSearchRequest bo, PageQuery pageQuery) {
        if (org.springframework.util.StringUtils.hasText(bo.getStartTime())) {
            boolean b = DateHelper.isValidDate(bo.getStartTime());
            if (!b) {
//                bo.setStartTime(bo.getStartTime() + " 00:00:00");
                bo.setStartTime("");
            }
        }
        if (org.springframework.util.StringUtils.hasText(bo.getEndTime())) {
            boolean b = DateHelper.isValidDate(bo.getEndTime());
            if (!b) {
//                bo.setEndTime(bo.getEndTime() + " 23:59:59");
                bo.setEndTime("");
            }
        }else{
            bo.setEndTime(bo.getStartTime());
        }

        LambdaQueryWrapper<OOrder> queryWrapper = new LambdaQueryWrapper<OOrder>()
                .eq(bo.getShopId() != null, OOrder::getShopId, bo.getShopId())
                .eq(bo.getMerchantId()!=null,OOrder::getMerchantId, bo.getMerchantId())
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderNum()), OOrder::getOrderNum, bo.getOrderNum())
                .eq(OOrder::getOrderStatus, 1)
//                .eq(bo.getRefundStatus()!=null,OOrder::getRefundStatus,bo.getRefundStatus())
                .eq(bo.getShopType() != null, OOrder::getShopType, bo.getShopType())
                .ge(org.springframework.util.StringUtils.hasText(bo.getStartTime()), OOrder::getOrderTime, bo.getStartTime()+ " 00:00:00")
                .le(org.springframework.util.StringUtils.hasText(bo.getEndTime()), OOrder::getOrderTime, bo.getEndTime()+ " 23:59:59")
                .ne(OOrder::getDistStatus, 2)//没有全部分配
                ;

        pageQuery.setOrderByColumn("order_time");
        pageQuery.setIsAsc("desc");
        Page<OOrder> pages = orderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if (pages.getRecords() != null) {
            for (OOrder order : pages.getRecords()) {
                order.setItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>()
                        .eq(OOrderItem::getOrderId, order.getId())
                        .eq(OOrderItem::getHasPushErp, 0)//还没有推送的
                ));

            }
        }

        return PageResult.build(pages);
    }

    @Override
    public ResultVo<Long> updateOrderStatus(String orderNo, Integer orderStatus, Integer refundStatus) {
        List<OOrder> shopOrders = orderMapper.selectList(new LambdaQueryWrapper<OOrder>().eq(OOrder::getOrderNum, orderNo));
        if (shopOrders != null && shopOrders.size() > 0) {
            OOrder update = new OOrder();
            update.setId(shopOrders.get(0).getId());
            update.setOrderStatus(orderStatus);
            update.setUpdateBy("更新订单状态");
            update.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(update);
            return ResultVo.success(Long.parseLong(shopOrders.get(0).getId()));
        }
        return ResultVo.error("未找到数据");
    }

    /**
     * 已经发货的list（去除分配给供应商发货的）
     *
     * @param bo
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult<OOrder> querySelfShippedPageList(OrderSearchRequest bo, PageQuery pageQuery) {
        LambdaQueryWrapper<OOrder> queryWrapper = new LambdaQueryWrapper<OOrder>()
                .eq(bo.getMerchantId() != null, OOrder::getMerchantId, bo.getMerchantId())
                .eq(bo.getShopId() != null, OOrder::getShopId, bo.getShopId())
                .eq(bo.getShopType() != null, OOrder::getShopType, bo.getShopType())
                .eq(OOrder::getShipStatus, 2)//发货状态 0 待发货 1 已分配供应商发货 2全部发货
                .lt(OOrder::getDistStatus, 2)//ship_type发货方 0 自己发货1联合发货2供应商发货
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderNum()), OOrder::getOrderNum, bo.getOrderNum());
        Page<OOrder> pages = orderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if (pages.getRecords() != null) {
            for (var order : pages.getRecords()) {
                order.setItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>()
                        .eq(OOrderItem::getOrderId, order.getId())
                        .eq(OOrderItem::getShipStatus, 2)
                        .eq(OOrderItem::getShipType, 0)
                        .eq(OOrderItem::getShipperId, 0)
                ));
            }
        }

        return PageResult.build(pages);
    }

    @Override
    public OOrder queryDetailAndCouponById(Long id) {
        OOrder oOrder = orderMapper.selectById(id);
        if (oOrder != null) {
//           oOrder.setItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, oOrder.getId())));
            oOrder.setItemVoList(orderItemMapper.selectOrderItemListByOrderId(id));
            // 获取发货记录(发货表，多个快递单号就是拆单发货)
            List<OOrderStocking> shipOrderList = shipOrderMapper.selectList(new LambdaQueryWrapper<OOrderStocking>().eq(OOrderStocking::getOrderNum, oOrder.getOrderNum()));
            oOrder.setLogistics(shipOrderList);

//            oOrder.setLogistics(shipMapper.selectList(new LambdaQueryWrapper<OShipment>().eq(OShipment::getOrderNums,oOrder.getOrderNum())));
            // 获取优惠信息
            if (oOrder.getShopType() == EnumShopType.TAO.getIndex()) {
                oOrder.setDiscounts(orderMapper.getTaoOrderDiscount(oOrder.getOrderNum()));
            } else if (oOrder.getShopType() == EnumShopType.JD.getIndex()) {
                oOrder.setDiscounts(orderMapper.getJdOrderDiscount(oOrder.getOrderNum()));
            } else if (oOrder.getShopType() == EnumShopType.PDD.getIndex()) {
                List<OrderDiscountVo> discountVoList = new ArrayList<>();
                if (oOrder.getPlatformDiscount() != null && oOrder.getPlatformDiscount() > 0) {
                    OrderDiscountVo vo = new OrderDiscountVo();
                    vo.setName("平台优惠");
                    vo.setDiscountAmount(oOrder.getPlatformDiscount().toString());
                    vo.setDescription("平台优惠");
                    discountVoList.add(vo);
                    oOrder.setDiscounts(discountVoList);
                }
            } else if (oOrder.getShopType() == EnumShopType.DOU.getIndex()) {
                List<OrderDiscountVo> discountVoList = new ArrayList<>();
                if (oOrder.getPlatformDiscount() != null && oOrder.getPlatformDiscount() > 0) {
                    OrderDiscountVo vo = new OrderDiscountVo();
                    vo.setName("平台优惠");
                    vo.setDiscountAmount(oOrder.getPlatformDiscount().toString());
                    vo.setDescription("平台优惠");
                    discountVoList.add(vo);
                    oOrder.setDiscounts(discountVoList);
                }
            }
            if(oOrder.getDiscounts()==null){
                oOrder.setDiscounts(new ArrayList<>());
            }
        }

        return oOrder;
    }

    @Override
    public OOrder queryDetailById(Long id) {
        OOrder oOrder = orderMapper.selectById(id);
        if (oOrder != null) {
            oOrder.setItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, oOrder.getId())));
//            oOrder.setItemVoList(orderItemMapper.selectOrderItemListByOrderId(id));
        }
        return oOrder;
    }

    @Override
    public OOrder queryDetailByOrderNum(String orderNum) {
        OOrder oOrder = orderMapper.selectList(new LambdaQueryWrapper<OOrder>().eq(OOrder::getOrderNum, orderNum)).stream().findFirst().orElse(null);
        if (oOrder != null) {
            oOrder.setItemList(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, oOrder.getId())));
        }
        return oOrder;
    }

    @Override
    public OOrder queryByOrderNum(String orderNum) {
        OOrder oOrder = orderMapper.selectList(new LambdaQueryWrapper<OOrder>().eq(OOrder::getOrderNum, orderNum)).stream().findFirst().orElse(null);
        return oOrder;
    }

    @Override
    public List<OOrder> searchOrderConsignee(String consignee) {
        LambdaQueryWrapper<OOrder> qw = new LambdaQueryWrapper<OOrder>().eq(OOrder::getOrderStatus, 1).likeRight(OOrder::getReceiverName, consignee);
        return orderMapper.selectList(qw);
    }

    @Override
    public List<OOrderItem> searchOrderItemByReceiverMobile(String receiverMobile) {
        List<OOrder> oOrders = orderMapper.selectList(new LambdaQueryWrapper<OOrder>().eq(OOrder::getOrderStatus, 1).eq(OOrder::getReceiverMobile, receiverMobile));
        List<OOrderItem> orderItemList = new ArrayList<>();
        if (oOrders != null) {
            for (OOrder order : oOrders) {
                orderItemList.addAll(orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, order.getId())));
            }
        }
        return orderItemList;
    }

    @Override
    public List<OOrderItem> queryItemList(String orderId) {
        return orderItemMapper.selectList(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, orderId));
    }

    /**
     * 待发货统计
     *
     * @param merchantId
     * @return
     */
    @Override
    public List<WaitShipReportVo> waitOrderReport(Long merchantId) {
        List<WaitShipReportVo> report = orderMapper.waitOrderReport(merchantId);
        return report;
    }

    @Override
    public List<SalesDailyVo> salesDaily() {
        return orderMapper.salesDaily(0L,0L);
    }

    @Override
    public SalesDailyVo getTodaySalesDaily(Long merchantId) {
        return orderMapper.getTodaySalesDaily(merchantId);
    }

    @Override
    public Integer getWaitShipOrderAllCount(Long merchantId) {
        return orderMapper.getWaitShipOrderAllCount(merchantId);
    }





    @Override
    public PageResult<OOrder> querySaleOrderPageList(OrderSearchRequest bo, PageQuery pageQuery) {
        if (StringUtils.hasText(bo.getStartTime())) {
            if (!DateHelper.isValidDate(bo.getStartTime())) {
                bo.setStartTime("");
            }
        }
        if (StringUtils.hasText(bo.getEndTime())) {
            if (!DateHelper.isValidDate(bo.getEndTime())) {
                bo.setEndTime("");
            }
        } else {
            bo.setEndTime(bo.getStartTime());
        }

        LambdaQueryWrapper<OOrder> queryWrapper = new LambdaQueryWrapper<OOrder>()
                .eq(StringUtils.hasText(bo.getOrderNum()), OOrder::getOrderNum, bo.getOrderNum())
                .eq(bo.getOrderStatus() != null, OOrder::getOrderStatus, bo.getOrderStatus())
                .ge(StringUtils.hasText(bo.getStartTime()), OOrder::getOrderTime, bo.getStartTime() + " 00:00:00")
                .le(StringUtils.hasText(bo.getEndTime()), OOrder::getOrderTime, bo.getEndTime() + " 23:59:59")
                .like(StringUtils.hasText(bo.getReceiverName()), OOrder::getReceiverName, bo.getReceiverName())
                .like(StringUtils.hasText(bo.getReceiverMobile()), OOrder::getReceiverMobile, bo.getReceiverMobile());

        pageQuery.setOrderByColumn("order_time");
        pageQuery.setIsAsc("desc");
        Page<OOrder> pages = orderMapper.selectPage(pageQuery.build(), queryWrapper);

        if (pages.getRecords() != null) {
            for (OOrder order : pages.getRecords()) {
                order.setItemVoList(orderItemMapper.selectOrderItemListByOrderId(Long.parseLong(order.getId())));
            }
        }

        return PageResult.build(pages);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Long> saveSaleOrder(OOrder order, List<OOrderItem> itemList, String username) {
        if (itemList == null || itemList.isEmpty()) {
            return ResultVo.error("请添加订单商品");
        }

        // 生成订单编号
        String orderNum = "SO" + System.currentTimeMillis();
        order.setOrderNum(orderNum);
        order.setOrderMode(1); // 手工订单
        order.setOrderStatus(0); // 新订单
        order.setShipStatus(0);
        order.setDistStatus(0);
        order.setHasGift(0);
        order.setCreateBy(username);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateBy(username);
        order.setUpdateTime(LocalDateTime.now());
        order.setOrderTime(LocalDateTime.now());

        // 计算金额
        double goodsAmount = 0;
        for (OOrderItem item : itemList) {
            goodsAmount += item.getGoodsPrice() * item.getQuantity();
        }
        order.setGoodsAmount(goodsAmount);
        if (order.getAmount() == null) {
            order.setAmount(goodsAmount);
        }
        if (order.getPayment() == null) {
            order.setPayment(goodsAmount);
        }

        orderMapper.insert(order);

        // 保存订单明细
        for (OOrderItem item : itemList) {
            item.setOrderId(order.getId());
            item.setOrderNum(orderNum);
            item.setSubOrderNum(orderNum);
            item.setCreateTime(LocalDateTime.now());
            item.setCreateBy(username);
            item.setUpdateTime(LocalDateTime.now());
            item.setUpdateBy(username);
            item.setOrderTime(LocalDateTime.now());
            item.setItemAmount(item.getGoodsPrice() * item.getQuantity());
            if (item.getRefundStatus() == null) {
                item.setRefundStatus(1);
            }
            if (item.getShipStatus() == null) {
                item.setShipStatus(0);
            }
            if (item.getHasPushErp() == null) {
                item.setHasPushErp(0);
            }
            if (item.getIsGift() == null) {
                item.setIsGift(0);
            }
            orderItemMapper.insert(item);
        }

        return ResultVo.success(Long.parseLong(order.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Long> updateSaleOrder(OOrder order, List<OOrderItem> itemList, String username) {
        OOrder existOrder = orderMapper.selectById(order.getId());
        if (existOrder == null) {
            return ResultVo.error("订单不存在");
        }
        if (existOrder.getOrderStatus() != 0 && existOrder.getOrderStatus() != 21) {
            return ResultVo.error("当前订单状态不允许修改");
        }

        order.setUpdateBy(username);
        order.setUpdateTime(LocalDateTime.now());
        order.setOrderMode(1);

        // 更新订单主表
        OOrder update = new OOrder();
        update.setId(order.getId());
        update.setRemark(order.getRemark());
        update.setReceiverName(order.getReceiverName());
        update.setReceiverMobile(order.getReceiverMobile());
        update.setDeliveryMethod(order.getDeliveryMethod());
        update.setAddress(order.getAddress());
        update.setProvince(order.getProvince());
        update.setCity(order.getCity());
        update.setTown(order.getTown());
        update.setUpdateBy(username);
        update.setUpdateTime(LocalDateTime.now());
        if (order.getAmount() != null) {
            update.setAmount(order.getAmount());
        }
        orderMapper.updateById(update);

        // 更新明细：先删后插
        if (itemList != null && !itemList.isEmpty()) {
            orderItemMapper.delete(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, order.getId()));

            double goodsAmount = 0;
            for (OOrderItem item : itemList) {
                item.setOrderId(order.getId());
                item.setOrderNum(existOrder.getOrderNum());
                item.setSubOrderNum(existOrder.getOrderNum());
                item.setCreateTime(LocalDateTime.now());
                item.setCreateBy(username);
                item.setUpdateTime(LocalDateTime.now());
                item.setUpdateBy(username);
                item.setOrderTime(existOrder.getOrderTime());
                item.setItemAmount(item.getGoodsPrice() * item.getQuantity());
                if (item.getRefundStatus() == null) {
                    item.setRefundStatus(1);
                }
                if (item.getShipStatus() == null) {
                    item.setShipStatus(0);
                }
                if (item.getHasPushErp() == null) {
                    item.setHasPushErp(0);
                }
                if (item.getIsGift() == null) {
                    item.setIsGift(0);
                }
                goodsAmount += item.getGoodsPrice() * item.getQuantity();
                orderItemMapper.insert(item);
            }

            OOrder amountUpdate = new OOrder();
            amountUpdate.setId(order.getId());
            amountUpdate.setGoodsAmount(goodsAmount);
            orderMapper.updateById(amountUpdate);
        }

        return ResultVo.success(Long.parseLong(order.getId()));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Long> removeSaleOrder(Long id) {
        OOrder order = orderMapper.selectById(id);
        if (order == null) {
            return ResultVo.error("订单不存在");
        }
        if (order.getOrderStatus() != 0 && order.getOrderStatus() != 21 && order.getOrderStatus() != 11) {
            return ResultVo.error("当前订单状态不允许删除");
        }

        // 删除明细
        orderItemMapper.delete(new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, id));
        // 删除主表
        orderMapper.deleteById(id);

        return ResultVo.success(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo cancelDouOrderMessage(String douOrderId) {

        List<OOrder> oOrders = orderMapper.selectList(new LambdaQueryWrapper<OOrder>().eq(OOrder::getOrderNum, douOrderId));
        if (!oOrders.isEmpty()) {
            for (var order : oOrders) {
                // 取消订单库
                OOrder update = new OOrder();
                update.setId(order.getId());
                update.setOrderStatus(11);
                update.setCancelReason("");
                update.setUpdateTime(LocalDateTime.now());
                update.setUpdateBy("平台通知取消订单");
                orderMapper.updateById(update);

                // 更新子订单
                OOrderItem item = new OOrderItem();
                item.setRefundStatus(4);
                item.setUpdateTime(LocalDateTime.now());
                item.setUpdateBy("平台通知取消订单");

                orderItemMapper.update(item, new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, order.getId()));

                // 取消发货订单
                List<OOrderStocking> oOrderStockings = shipOrderMapper.selectList(new LambdaQueryWrapper<OOrderStocking>().eq(OOrderStocking::getOOrderId, order.getId()));
                if (!oOrderStockings.isEmpty()) {
                    for (OOrderStocking oOrderStocking : oOrderStockings) {
                        OOrderStocking oOrderStockingUpdate = new OOrderStocking();
                        oOrderStockingUpdate.setId(oOrderStocking.getId());
                        oOrderStockingUpdate.setOrderStatus(11);
                        oOrderStockingUpdate.setUpdateBy("取消订单");
                        oOrderStockingUpdate.setUpdateTime(LocalDateTime.now());
                        shipOrderMapper.updateById(oOrderStockingUpdate);

                        // 取消子订单
                        OOrderStockingItem shipItem = new OOrderStockingItem();
                        shipItem.setRefundStatus(4);
                        shipItem.setUpdateTime(LocalDateTime.now());
                        shipItem.setUpdateBy("平台通知取消订单");
                        shipOrderItemMapper.update(shipItem,new LambdaQueryWrapper<OOrderStockingItem>().eq(OOrderStockingItem::getShipOrderId,oOrderStocking.getId()));
                    }
                }
                log.info("======取消订单成功=======");
            }
        }
        return ResultVo.success();
    }

    @Override
    public void insertOrderItem(OOrderItem orderItem) {
        orderItemMapper.insert(orderItem);
    }

    @Override
    public PageResult<OOrder> queryPageListBySource(OOrder query, PageQuery pageQuery) {
        LambdaQueryWrapper<OOrder> qw = new LambdaQueryWrapper<>();
        qw.eq(StringUtils.hasText(query.getOrderSource()), OOrder::getOrderSource, query.getOrderSource());
        qw.eq(query.getShopId() != null, OOrder::getShopId, query.getShopId());
        qw.orderByDesc(OOrder::getCreateTime);
        Page<OOrder> page = orderMapper.selectPage(pageQuery.build(), qw);
        return PageResult.build(page);
    }

    @Override
    public List<OOrderItem> selectItemsByOrderId(String orderId) {
        return orderItemMapper.selectList(
            new LambdaQueryWrapper<OOrderItem>().eq(OOrderItem::getOrderId, orderId));
    }

    @Override
    public void updateOrderItem(OOrderItem orderItem) {
        orderItemMapper.updateById(orderItem);
    }

}





