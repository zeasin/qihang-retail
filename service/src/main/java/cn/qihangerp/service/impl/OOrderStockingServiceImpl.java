package cn.qihangerp.service.impl;

import cn.qihangerp.common.DateHelper;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.enums.*;
import cn.qihangerp.mapper.*;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.bo.StockingOrderBo;
import cn.qihangerp.request.ShipRecordQueryRequest;
import cn.qihangerp.request.SupplierShipOrderSearchRequest;
import cn.qihangerp.service.OOrderItemService;
import cn.qihangerp.service.OOrderService;
import cn.qihangerp.service.OGoodsSkuService;
import cn.qihangerp.service.OGoodsService;
import cn.qihangerp.service.OOrderStockingService;
import cn.qihangerp.service.OOrderStockingItemService;
import cn.qihangerp.service.ErpWarehouseService;
import cn.qihangerp.utils.DateUtils;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

/**
* @author qilip
* @description 针对表【o_supplier_ship_order(供应商发货订单)】的数据库操作Service实现
* @createDate 2025-02-18 11:56:14
*/
@Slf4j
@AllArgsConstructor
@Service
public class OOrderStockingServiceImpl extends ServiceImpl<OOrderStockingMapper, OOrderStocking>
    implements OOrderStockingService {
    private final OOrderItemService orderItemService;
    private final OOrderService orderService;
    private final OGoodsSkuService goodsSkuService;
    private final OGoodsService goodsService;
    private final OOrderStockingMapper shipOrderMapper;
    private final OOrderStockingItemService shipOrderItemService;
    private final ErpWarehouseService erpWarehouseService;




    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<String> pushOrderToCloudWarehouse(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return ResultVo.error("请选择要推送的订单");
        }
        int success = 0;
        int fail = 0;
        for (Long orderId : orderIds) {
            try {
                OOrder order = orderService.getById(orderId);
                if (order == null) {
                    log.error("推送云仓失败：订单不存在，orderId={}", orderId);
                    fail++;
                    continue;
                }
                if (order.getOrderStatus() != 1) {
                    log.error("推送云仓失败：订单状态不是待发货，orderId={}", orderId);
                    fail++;
                    continue;
                }
                if (order.getShipStatus() != 0 && order.getShipStatus() != null) {
                    log.error("推送云仓失败：订单已处理，orderId={}", orderId);
                    fail++;
                    continue;
                }
                if (order.getDistStatus() != null && order.getDistStatus() == 2) {
                    log.error("推送云仓失败：订单已全部分配，orderId={}", orderId);
                    fail++;
                    continue;
                }

                List<OOrderItem> items = orderItemService.list(new LambdaQueryWrapper<OOrderItem>()
                        .eq(OOrderItem::getOrderId, orderId)
                        .eq(OOrderItem::getRefundStatus, 1)
                        .eq(OOrderItem::getShipStatus, 0)
                );
                if (items == null || items.isEmpty()) {
                    log.error("推送云仓失败：没有待发货的item，orderId={}", orderId);
                    fail++;
                    continue;
                }

                OOrderStocking stocking = new OOrderStocking();
                stocking.setType(EnumShipType.CLOUD_WAREHOUSE.getIndex());
                stocking.setMerchantId(order.getMerchantId());
                stocking.setOOrderId(Long.parseLong(order.getId()));
                stocking.setOrderNum(order.getOrderNum());
                stocking.setOrderTime(order.getOrderTime());
                stocking.setShopType(order.getShopType());
                stocking.setShopId(order.getShopId());
                stocking.setShipMode(0);
                stocking.setRemark(order.getRemark());
                stocking.setBuyerMemo(order.getBuyerMemo());
                stocking.setSellerMemo(order.getSellerMemo());
                stocking.setSendStatus(1);
                stocking.setOrderStatus(order.getOrderStatus());
                stocking.setWaybillStatus(0);
                stocking.setStockingStatus(0);
                stocking.setOrderType(0);
                stocking.setErpPushStatus(1);
                stocking.setErpPushResult("SUCCESS");
                stocking.setCreateTime(LocalDateTime.now());
                stocking.setCreateBy("推送云仓发货");
                stocking.setProvince(order.getProvince());
                stocking.setCity(order.getCity());
                stocking.setTown(order.getTown());
                stocking.setAddress(order.getAddress());
                stocking.setReceiverName(order.getReceiverName());
                stocking.setReceiverMobile(order.getReceiverMobile());
                shipOrderMapper.insert(stocking);

                for (OOrderItem item : items) {
                    OOrderStockingItem stockingItem = new OOrderStockingItem();
                    stockingItem.setShipOrderId(stocking.getId());
                    stockingItem.setMerchantId(order.getMerchantId());
                    stockingItem.setOOrderId(Long.parseLong(order.getId()));
                    stockingItem.setOOrderItemId(Long.parseLong(item.getId()));
                    stockingItem.setOrderNum(item.getOrderNum());
                    stockingItem.setSubOrderNum(item.getSubOrderNum());
                    stockingItem.setSkuId(item.getSkuId());
                    stockingItem.setProductId(item.getProductId());
                    stockingItem.setGoodsId(item.getGoodsId());
                    stockingItem.setGoodsSkuId(item.getGoodsSkuId());
                    stockingItem.setRefundStatus(item.getRefundStatus());
                    stockingItem.setGoodsName(item.getGoodsTitle());
                    stockingItem.setGoodsNum(item.getGoodsNum());
                    stockingItem.setGoodsImg(item.getGoodsImg());
                    stockingItem.setSkuName(item.getGoodsSpec());
                    stockingItem.setSkuCode(item.getSkuNum());
                    stockingItem.setBarcode(item.getBarcode());
                    stockingItem.setQuantity(item.getQuantity());
                    stockingItem.setUnshippedQuantity(item.getQuantity());
                    stockingItem.setSendStatus(0);
                    stockingItem.setOrderTime(item.getOrderTime());
                    stockingItem.setCreateTime(LocalDateTime.now());
                    stockingItem.setCreateBy("推送云仓发货");
                    shipOrderItemService.save(stockingItem);

                    OOrderItem updateItem = new OOrderItem();
                    updateItem.setId(item.getId());
                    updateItem.setShipType(EnumShipType.CLOUD_WAREHOUSE.getIndex());
                    updateItem.setShipStatus(1);
                    updateItem.setHasPushErp(1);
                    updateItem.setUpdateTime(LocalDateTime.now());
                    updateItem.setUpdateBy("推送云仓发货");
                    orderItemService.updateById(updateItem);
                }

                long unassignedCount = orderItemService.count(new LambdaQueryWrapper<OOrderItem>()
                        .eq(OOrderItem::getOrderId, orderId)
                        .eq(OOrderItem::getRefundStatus, 1)
                        .eq(OOrderItem::getShipStatus, 0)
                );
                OOrder updateOrder = new OOrder();
                updateOrder.setId(order.getId());
                if (unassignedCount == 0) {
                    updateOrder.setDistStatus(2);
                } else {
                    updateOrder.setDistStatus(1);
                }
                orderService.updateById(updateOrder);

                success++;
                log.info("推送云仓成功：orderId={}, orderNum={}", orderId, order.getOrderNum());
            } catch (Exception e) {
                log.error("推送云仓异常：orderId={}", orderId, e);
                fail++;
            }
        }
        String msg = "推送完成，成功：" + success + "，失败：" + fail;
        if (fail > 0) return ResultVo.error(msg);
        return ResultVo.success(msg);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<String> pushOrderItemToCloudWarehouseByIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return ResultVo.error("请选择要推送的商品");
        }
        List<OOrderItem> selectedItems = orderItemService.listByIds(itemIds.stream().map(String::valueOf).collect(Collectors.toList()));
        if (selectedItems == null || selectedItems.isEmpty()) {
            return ResultVo.error("未找到订单商品数据");
        }
        Map<String, List<OOrderItem>> groupedByOrder = selectedItems.stream()
                .collect(Collectors.groupingBy(OOrderItem::getOrderId));

        int success = 0;
        int fail = 0;
        for (Map.Entry<String, List<OOrderItem>> entry : groupedByOrder.entrySet()) {
            String orderId = entry.getKey();
            List<OOrderItem> items = entry.getValue();
            try {
                OOrder order = orderService.getById(orderId);
                if (order == null) {
                    log.error("推送云仓失败：订单不存在，orderId={}", orderId);
                    fail += items.size();
                    continue;
                }
                if (order.getOrderStatus() != 1) {
                    log.error("推送云仓失败：订单状态不是待发货，orderId={}", orderId);
                    fail += items.size();
                    continue;
                }

                List<OOrderStocking> existStockings = shipOrderMapper.selectList(new LambdaQueryWrapper<OOrderStocking>()
                        .eq(OOrderStocking::getOOrderId, Long.parseLong(orderId))
                        .eq(OOrderStocking::getType, EnumShipType.CLOUD_WAREHOUSE.getIndex())
                );

                OOrderStocking stocking;
                if (existStockings != null && !existStockings.isEmpty()) {
                    stocking = existStockings.get(0);
                    log.info("订单已有备货单，追加商品：orderId={}, stockingId={}", orderId, stocking.getId());
                } else {
                    stocking = new OOrderStocking();
                    stocking.setType(EnumShipType.CLOUD_WAREHOUSE.getIndex());
                    stocking.setMerchantId(order.getMerchantId());
                    stocking.setOOrderId(Long.parseLong(order.getId()));
                    stocking.setOrderNum(order.getOrderNum());
                    stocking.setOrderTime(order.getOrderTime());
                    stocking.setShopType(order.getShopType());
                    stocking.setShopId(order.getShopId());
                    stocking.setShipMode(0);
                    stocking.setRemark(order.getRemark());
                    stocking.setBuyerMemo(order.getBuyerMemo());
                    stocking.setSellerMemo(order.getSellerMemo());
                    stocking.setSendStatus(1);
                    stocking.setOrderStatus(order.getOrderStatus());
                    stocking.setWaybillStatus(0);
                    stocking.setStockingStatus(0);
                    stocking.setOrderType(0);
                    stocking.setErpPushStatus(1);
                    stocking.setErpPushResult("SUCCESS");
                    stocking.setCreateTime(LocalDateTime.now());
                    stocking.setCreateBy("推送云仓发货");
                    stocking.setProvince(order.getProvince());
                    stocking.setCity(order.getCity());
                    stocking.setTown(order.getTown());
                    stocking.setAddress(order.getAddress());
                    stocking.setReceiverName(order.getReceiverName());
                    stocking.setReceiverMobile(order.getReceiverMobile());
                    shipOrderMapper.insert(stocking);
                }

                for (OOrderItem item : items) {
                    long existCount = shipOrderItemService.count(new LambdaQueryWrapper<OOrderStockingItem>()
                            .eq(OOrderStockingItem::getOOrderItemId, Long.parseLong(item.getId()))
                            .eq(OOrderStockingItem::getShipOrderId, stocking.getId())
                    );
                    if (existCount > 0) {
                        log.info("商品已推送，跳过：itemId={}", item.getId());
                        continue;
                    }

                    OOrderStockingItem stockingItem = new OOrderStockingItem();
                    stockingItem.setShipOrderId(stocking.getId());
                    stockingItem.setMerchantId(order.getMerchantId());
                    stockingItem.setOOrderId(Long.parseLong(order.getId()));
                    stockingItem.setOOrderItemId(Long.parseLong(item.getId()));
                    stockingItem.setOrderNum(item.getOrderNum());
                    stockingItem.setSubOrderNum(item.getSubOrderNum());
                    stockingItem.setSkuId(item.getSkuId());
                    stockingItem.setProductId(item.getProductId());
                    stockingItem.setGoodsId(item.getGoodsId());
                    stockingItem.setGoodsSkuId(item.getGoodsSkuId());
                    stockingItem.setRefundStatus(item.getRefundStatus());
                    stockingItem.setGoodsName(item.getGoodsTitle());
                    stockingItem.setGoodsNum(item.getGoodsNum());
                    stockingItem.setGoodsImg(item.getGoodsImg());
                    stockingItem.setSkuName(item.getGoodsSpec());
                    stockingItem.setSkuCode(item.getSkuNum());
                    stockingItem.setBarcode(item.getBarcode());
                    stockingItem.setQuantity(item.getQuantity());
                    stockingItem.setUnshippedQuantity(item.getQuantity());
                    stockingItem.setSendStatus(0);
                    stockingItem.setOrderTime(item.getOrderTime());
                    stockingItem.setCreateTime(LocalDateTime.now());
                    stockingItem.setCreateBy("推送云仓发货");
                    shipOrderItemService.save(stockingItem);

                    OOrderItem updateItem = new OOrderItem();
                    updateItem.setId(item.getId());
                    updateItem.setShipType(EnumShipType.CLOUD_WAREHOUSE.getIndex());
                    updateItem.setShipStatus(1);
                    updateItem.setHasPushErp(1);
                    updateItem.setUpdateTime(LocalDateTime.now());
                    updateItem.setUpdateBy("推送云仓发货");
                    orderItemService.updateById(updateItem);

                    success++;
                }

                long unassignedCount = orderItemService.count(new LambdaQueryWrapper<OOrderItem>()
                        .eq(OOrderItem::getOrderId, orderId)
                        .eq(OOrderItem::getRefundStatus, 1)
                        .eq(OOrderItem::getShipStatus, 0)
                );
                OOrder updateOrder = new OOrder();
                updateOrder.setId(order.getId());
                if (unassignedCount == 0) {
                    updateOrder.setDistStatus(2);
                } else {
                    updateOrder.setDistStatus(1);
                }
                orderService.updateById(updateOrder);

                log.info("推送云仓成功：orderId={}, orderNum={}", orderId, order.getOrderNum());
            } catch (Exception e) {
                log.error("推送云仓异常：orderId={}", orderId, e);
                fail += items.size();
            }
        }
        String msg = "推送完成，成功：" + success + "，失败：" + fail;
        if (fail > 0) return ResultVo.error(msg);
        return ResultVo.success(msg);
    }





    @Override
    public PageResult<OOrderStocking> queryPageList(SupplierShipOrderSearchRequest bo, PageQuery pageQuery) {
        if(StringUtils.hasText(bo.getStartTime())){
            boolean b = DateHelper.isValidDate(bo.getStartTime());
            if(!b){
//                bo.setStartTime(bo.getStartTime()+" 00:00:00");
                bo.setStartTime("");
            }
        }
        if(StringUtils.hasText(bo.getEndTime())){
            boolean b = DateHelper.isValidDate(bo.getEndTime());
            if(!b){
//                bo.setEndTime(bo.getEndTime()+" 23:59:59");
                bo.setEndTime("");
            }
        }else{
            bo.setEndTime(bo.getStartTime());
        }
//        if(bo.getOrderStatus()==null) bo.setOrderStatus(1);

        LambdaQueryWrapper<OOrderStocking> queryWrapper = new LambdaQueryWrapper<OOrderStocking>()
                .eq(bo.getType()!=null,OOrderStocking::getType,bo.getType())
                .eq(bo.getMerchantId()!=null, OOrderStocking::getMerchantId,bo.getMerchantId())
                .eq(bo.getSupplierId()!=null, OOrderStocking::getShipperId,bo.getSupplierId())
                .eq(bo.getStockingStatus()!=null, OOrderStocking::getStockingStatus,bo.getStockingStatus())
                .eq(bo.getShopId()!=null, OOrderStocking::getShopId,bo.getShopId())
                .eq(bo.getShopType()!=null, OOrderStocking::getShopType,bo.getShopType())
                .eq(StringUtils.hasText(bo.getOrderNum()), OOrderStocking::getOrderNum,bo.getOrderNum())
                .eq(bo.getSendStatus()!=null&&bo.getSendStatus()!=10, OOrderStocking::getSendStatus,bo.getSendStatus())
                .lt(bo.getSendStatus()!=null&&bo.getSendStatus()==10, OOrderStocking::getSendStatus,EnumShipStatus.ALL.getIndex())
                .eq(bo.getSendStatus()!=null, OOrderStocking::getOrderStatus,1)
                .eq(bo.getWaybillStatus()!=null, OOrderStocking::getWaybillStatus,bo.getWaybillStatus())
//                .eq(bo.getPlatformId()!=null, OOrderStocking::getShopType,bo.getPlatformId())
                .ge(StringUtils.hasText(bo.getStartTime()), OOrderStocking::getOrderTime,bo.getStartTime()+" 00:00:00")
                .le(StringUtils.hasText(bo.getEndTime()), OOrderStocking::getOrderTime,bo.getEndTime()+" 23:59:59")
                .eq(bo.getOrderStatus()!=null, OOrderStocking::getOrderStatus,bo.getOrderStatus());

//        pageQuery.setOrderByColumn("order_time");
//        pageQuery.setIsAsc("desc");
        Page<OOrderStocking> pages = shipOrderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if(pages.getRecords()!=null){
            for (OOrderStocking order:pages.getRecords()) {
                order.setItemList(shipOrderItemService.list(new LambdaQueryWrapper<OOrderStockingItem>()
                        .eq(OOrderStockingItem::getShipOrderId, order.getId())
                                .lt(bo.getSendStatus()!=null&&bo.getSendStatus()==10, OOrderStockingItem::getSendStatus,EnumShipStatus.ALL.getIndex())
                        )
                );
            }
        }

        return PageResult.build(pages);
    }

    /**
     * 已分配给供应商发货的订单list
     * @param bo
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult<OOrderStocking> querySupplierShipPageList(SupplierShipOrderSearchRequest bo, PageQuery pageQuery) {
        if(StringUtils.hasText(bo.getStartTime())){
            boolean b = DateHelper.isValidDate(bo.getStartTime());
            if(!b){
//                bo.setStartTime(bo.getStartTime()+" 00:00:00");
                bo.setStartTime("");
            }
        }
        if(StringUtils.hasText(bo.getEndTime())){
            boolean b = DateHelper.isValidDate(bo.getEndTime());
            if(!b){
//                bo.setEndTime(bo.getEndTime()+" 23:59:59");
                bo.setEndTime("");
            }
        }else {
            bo.setEndTime(bo.getStartTime());
        }
//        if(bo.getOrderStatus()==null) bo.setOrderStatus(1);

        LambdaQueryWrapper<OOrderStocking> queryWrapper = new LambdaQueryWrapper<OOrderStocking>()
                .eq(OOrderStocking::getType,EnumShipType.SUPPLIER.getIndex())
                .eq(bo.getSupplierId()!=null, OOrderStocking::getShipperId,bo.getSupplierId())
                .eq(bo.getWarehouseId()!=null, OOrderStocking::getWarehouseId,bo.getWarehouseId())
                .eq(bo.getMerchantId()!=null,OOrderStocking::getMerchantId,bo.getMerchantId())
                .eq(bo.getShopId()!=null, OOrderStocking::getShopId,bo.getShopId())
                .eq(StringUtils.hasText(bo.getOrderNum()), OOrderStocking::getOrderNum,bo.getOrderNum())
                .eq(bo.getSendStatus()!=null, OOrderStocking::getSendStatus,bo.getSendStatus())
                .eq(bo.getWaybillStatus()!=null, OOrderStocking::getWaybillStatus,bo.getWaybillStatus())
                .eq(bo.getShopType()!=null, OOrderStocking::getShopType,bo.getShopType())
                .ge(StringUtils.hasText(bo.getStartTime()), OOrderStocking::getOrderTime,bo.getStartTime()+" 00:00:00")
                .le(StringUtils.hasText(bo.getEndTime()), OOrderStocking::getOrderTime,bo.getEndTime()+" 23:59:59")
                .eq(bo.getOrderStatus()!=null, OOrderStocking::getOrderStatus,bo.getOrderStatus());

//        pageQuery.setOrderByColumn("order_time");
//        pageQuery.setIsAsc("desc");
        Page<OOrderStocking> pages = shipOrderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if(pages.getRecords()!=null){
            for (OOrderStocking order:pages.getRecords()) {
                order.setItemList(shipOrderItemService.list(new LambdaQueryWrapper<OOrderStockingItem>().eq(OOrderStockingItem::getShipOrderId, order.getId())));
            }
        }

        return PageResult.build(pages);
    }

    /**
     * 仓库备货list（仓库一定是发了货才会出现在备货列表）
     * @param request
     * @param pageQuery
     * @return
     */
    @Override
    public PageResult<OOrderStocking> queryStockUpPageList(StockingOrderBo request, PageQuery pageQuery) {
        LambdaQueryWrapper<OOrderStocking> queryWrapper = new LambdaQueryWrapper<OOrderStocking>()
                .eq(OOrderStocking::getType,EnumShipType.LOCAL.getIndex())//类型：0本地仓库备货  10供应商发货 20商户发货 100京东云仓发货
//                .eq( OOrderStocking::getSupplierId,0)//0代表仓库发货的，仓库发货的一定是发了货才会到这里的
                .eq(OOrderStocking::getMerchantId,request.getMerchantId())
                .eq(StringUtils.hasText(request.getOrderNum()), OOrderStocking::getOrderNum,request.getOrderNum())
                .eq(request.getStockingStatus()!=null, OOrderStocking::getStockingStatus,request.getStockingStatus())
//                .lt(request.getStockingStatus()==null, OOrderStocking::getStockingStatus,2)
                ;

        pageQuery.setOrderByColumn("order_time");
        pageQuery.setIsAsc("desc");
        Page<OOrderStocking> pages = shipOrderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if(pages.getRecords()!=null){
            for (OOrderStocking order:pages.getRecords()) {
                order.setItemList(shipOrderItemService.list(new LambdaQueryWrapper<OOrderStockingItem>().eq(OOrderStockingItem::getShipOrderId, order.getId())));
            }
        }

        return PageResult.build(pages);
    }
    @Override
    public OOrderStocking queryDetailById(Long id) {
        OOrderStocking shipOrder = shipOrderMapper.selectById(id);
        if(shipOrder!=null){
            shipOrder.setItemList(shipOrderItemService.list(new LambdaQueryWrapper<OOrderStockingItem>().eq(OOrderStockingItem::getShipOrderId, shipOrder.getId())));
        }
        return shipOrder;
    }

    @Override
    public List<OOrderStockingItem> getItemsByOrderId(Long shipOrderId) {
        List<OOrderStockingItem> list = shipOrderItemService.list(
                new LambdaQueryWrapper<OOrderStockingItem>()
                        .eq(OOrderStockingItem::getShipOrderId, shipOrderId)
                        .eq(OOrderStockingItem::getRefundStatus,1)
        );
        return list;
    }

    @Override
    public List<OOrderStockingItem> getItemsByOrderNum(String orderNum) {
        List<OOrderStockingItem> list = shipOrderItemService.list(
                new LambdaQueryWrapper<OOrderStockingItem>()
                        .eq(OOrderStockingItem::getOrderNum, orderNum)
        );
        return list;
    }

    @Override
    public List<OOrderStocking> getByOrderNum(String orderNum) {
        return shipOrderMapper.selectList(new LambdaQueryWrapper<OOrderStocking>().eq(OOrderStocking::getOrderNum, orderNum));
}

    /**
     * 仓库系统 生成出库单（按发货订单）
     * @param stockingId
     * @return
     */
//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public ResultVo warehouseGenerateStockOutByShipOrder(Long stockingId) {
//        if(stockingId==null||stockingId==0) return ResultVo.error("参数错误：Id不能为空");
//
//        OOrderStocking oOrderStocking = shipOrderMapper.selectById(stockingId);
//        if(oOrderStocking==null) return ResultVo.error("找不到发货数据");
//        else if (oOrderStocking.getStockingStatus().intValue()!=0) {
//            return ResultVo.error("发货单已经生成出库单了");
//        }
//
//        List<OOrderStockingItem> oOrderStockingItems = shipOrderItemService.list(new LambdaQueryWrapper<OOrderStockingItem>().eq(OOrderStockingItem::getShipOrderId, stockingId));
//        if(oOrderStockingItems.isEmpty()) return ResultVo.error("找不到发货item数据");
//
//
//        // 出库单item
//        List<ErpWarehouseStockOutItem> stockOutItemList = new ArrayList<>();
//
//        int total=0;
//        for(var oOrderStockingItem :oOrderStockingItems){
//            // 查询 仓库商品数据
//            List<ErpWarehouseGoods> erpWarehouseGoods = warehouseGoodsMapper.selectList(new LambdaQueryWrapper<ErpWarehouseGoods>()
//                    .eq(ErpWarehouseGoods::getErpGoodsSkuId, oOrderStockingItem.getGoodsSkuId())
//                    .eq(ErpWarehouseGoods::getWarehouseId,oOrderStocking.getWarehouseId())
//            );
//            if(erpWarehouseGoods.isEmpty()) return ResultVo.error("仓库没有找到该商品");
//
//            // 组合出库单item数据
//            ErpWarehouseStockOutItem stockOutItem = new ErpWarehouseStockOutItem();
//            stockOutItem.setType(1);//出库类型1订单拣货出库2采购退货出库3盘点出库4报损出库
////            stockOutItem.setSourceOrderId(oOrderStockingItem.getShipOrderId());
////            stockOutItem.setSourceOrderItemId(oOrderStockingItem.getId());
////            stockOutItem.setSourceOrderNum(oOrderStockingItem.getOrderNum());
//            stockOutItem.setOriginalQuantity(oOrderStockingItem.getQuantity());
//            stockOutItem.setOutQuantity(0);
//            stockOutItem.setStatus(0);//状态：0待出库1部分出库2全部出库
//            stockOutItem.setWarehouseId(0L);
////            stockOutItem.setPositionId(0L);
//            stockOutItem.setMerchantId(oOrderStockingItem.getMerchantId());
//            stockOutItem.setGoodsId(erpWarehouseGoods.get(0).getId());
//            stockOutItem.setGoodsNum(erpWarehouseGoods.get(0).getGoodsNo());
//            stockOutItem.setGoodsName(erpWarehouseGoods.get(0).getGoodsName());
//            stockOutItem.setGoodsImage(erpWarehouseGoods.get(0).getImageUrl());
//            stockOutItem.setSkuName(erpWarehouseGoods.get(0).getStandard());
//            stockOutItem.setCreateTime(LocalDateTime.now());
//            stockOutItem.setUpdateTime(LocalDateTime.now());
//            stockOutItem.setVendorId(oOrderStocking.getWarehouseId());
//            total+= oOrderStockingItem.getQuantity();
//            stockOutItemList.add(stockOutItem);
//
//            //更新自己 备货状态
//            OOrderStockingItem up = new OOrderStockingItem();
//            up.setId(oOrderStockingItem.getId());
//            up.setStockingStatus(1);//状态0待备货(待出库)1部分备货(出库中)2全部备货(已出库)
//            up.setUpdateTime(LocalDateTime.now());
//            up.setUpdateBy("生成出库单");
//            shipOrderItemService.updateById(up);
//        }
//
//        // 插入出库单
//        ErpWarehouseStockOut stockOut = new ErpWarehouseStockOut();
//        stockOut.setOutNum(DateUtils.getCurrentDateTime());
//        stockOut.setType(1);//出库类型1订单拣货出库2采购退货出库3盘点出库4报损出库
//        stockOut.setSourceId(oOrderStocking.getId());
//        stockOut.setSourceNum(oOrderStocking.getOrderNum());
//        stockOut.setGoodsUnit(oOrderStockingItems.size());
//        stockOut.setSpecUnit(oOrderStockingItems.size());
//        stockOut.setSpecUnitTotal(total);
//        stockOut.setOutTotal(0);
//        stockOut.setStatus(0);
//        stockOut.setPrintStatus(0);
//        stockOut.setCreateBy("发货订单生成出库单");
//        stockOut.setCreateTime(LocalDateTime.now());
//        stockOut.setVendorId(oOrderStocking.getWarehouseId());
//        stockOut.setMerchantId(oOrderStocking.getMerchantId());
//        warehouseStockOutMapper.insert(stockOut);
//
//        for(var it:stockOutItemList){
//            it.setEntryId(stockOut.getId());
//            warehouseStockOutItemMapper.insert(it);
//        }
//        // 云仓发货 生成出库单
//        OOrderStocking up= new OOrderStocking();
//        up.setId(stockingId);
//        up.setStockingStatus(1);
//        up.setUpdateBy("生成出库单");
//        up.setUpdateTime(LocalDateTime.now());
//        shipOrderMapper.updateById(up);
//        return ResultVo.success();
//    }

    /**
     * 统一发货记录查询
     * 根据 type 区分：0=本地仓，300=供应商，100/110/200=云仓
     */
    @Override
    public PageResult<OOrderStocking> queryShipRecordPageList(ShipRecordQueryRequest request, PageQuery pageQuery) {
        // 时间校验
        if(StringUtils.hasText(request.getStartTime())){
            boolean b = DateHelper.isValidDate(request.getStartTime());
            if(!b) request.setStartTime("");
        }
        if(StringUtils.hasText(request.getEndTime())){
            boolean b = DateHelper.isValidDate(request.getEndTime());
            if(!b) request.setEndTime("");
        }else if(StringUtils.hasText(request.getStartTime())){
            request.setEndTime(request.getStartTime());
        }

        Integer type = request.getType();
        LambdaQueryWrapper<OOrderStocking> queryWrapper = new LambdaQueryWrapper<OOrderStocking>();

        // 按 type 构建基础过滤条件
        if(type != null){
            if(type == EnumShipType.SUPPLIER.getIndex()){
                // 供应商发货
                queryWrapper.eq(OOrderStocking::getType, EnumShipType.SUPPLIER.getIndex())
                        .eq(request.getSupplierId()!=null, OOrderStocking::getShipperId, request.getSupplierId());
            } else if(type >= EnumShipType.JD_CLOUD_WAREHOUSE.getIndex() && type <= EnumShipType.CLOUD_WAREHOUSE.getIndex()){
                // 云仓发货（精确匹配type）
                queryWrapper.eq(OOrderStocking::getType, type);
            } else if(type == EnumShipType.LOCAL.getIndex()){
                // 本地仓发货
                queryWrapper.eq(OOrderStocking::getType, EnumShipType.LOCAL.getIndex());
            } else {
                // 类型不匹配，返回空结果
                queryWrapper.eq(OOrderStocking::getId, -1L);
            }
        } else if(Boolean.TRUE.equals(request.getAllCloud())){
            // type 为空但 allCloud=true：查询全部云仓发货（京东云仓/吉客云/系统云仓）
            queryWrapper.ge(OOrderStocking::getType, 100).le(OOrderStocking::getType, 200);
        } else {
            // type 未指定，allCloud 也未指定：默认不限制 type
        }

        // 通用查询条件
        queryWrapper
                .eq(request.getShipperId()!=null && type!=null && type==EnumShipType.LOCAL.getIndex(), OOrderStocking::getShipperId, request.getShipperId())
                .eq(request.getMerchantId()!=null, OOrderStocking::getMerchantId, request.getMerchantId())
                .eq(request.getShopId()!=null, OOrderStocking::getShopId, request.getShopId())
                .eq(StringUtils.hasText(request.getOrderNum()), OOrderStocking::getOrderNum, request.getOrderNum())
                .eq(StringUtils.hasText(request.getWaybillCode()), OOrderStocking::getWaybillCode, request.getWaybillCode())
                .eq(request.getSendStatus()!=null, OOrderStocking::getSendStatus, request.getSendStatus())
                .eq(request.getWaybillStatus()!=null, OOrderStocking::getWaybillStatus, request.getWaybillStatus())
                .eq(request.getShopType()!=null, OOrderStocking::getShopType, request.getShopType())
                .eq(request.getStockingStatus()!=null, OOrderStocking::getStockingStatus, request.getStockingStatus())
                .eq(request.getOrderStatus()!=null, OOrderStocking::getOrderStatus, request.getOrderStatus())
                // 云仓专用条件
                .eq(request.getErpPushStatus()!=null, OOrderStocking::getErpPushStatus, request.getErpPushStatus())
                .eq(StringUtils.hasText(request.getShippingErpOrderCode()), OOrderStocking::getShippingErpOrderCode, request.getShippingErpOrderCode())
                .eq(StringUtils.hasText(request.getShippingOrderCode()), OOrderStocking::getShippingOrderCode, request.getShippingOrderCode())
                // 时间范围：本地仓/供应商用 orderTime，云仓用 createTime
                .ge(StringUtils.hasText(request.getStartTime()) && type!=null && type==EnumShipType.SUPPLIER.getIndex(),
                        OOrderStocking::getOrderTime, request.getStartTime()+" 00:00:00")
                .le(StringUtils.hasText(request.getEndTime()) && type!=null && type==EnumShipType.SUPPLIER.getIndex(),
                        OOrderStocking::getOrderTime, request.getEndTime()+" 23:59:59")
                .ge(StringUtils.hasText(request.getStartTime()) && (type!=null && type>=100 || Boolean.TRUE.equals(request.getAllCloud())),
                        OOrderStocking::getCreateTime, request.getStartTime()+" 00:00:00")
                .le(StringUtils.hasText(request.getEndTime()) && (type!=null && type>=100 || Boolean.TRUE.equals(request.getAllCloud())),
                        OOrderStocking::getCreateTime, request.getEndTime()+" 23:59:59")
                .orderByDesc(OOrderStocking::getId);

        Page<OOrderStocking> pages = shipOrderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if(pages.getRecords()!=null){
            for (OOrderStocking order:pages.getRecords()) {
                order.setItemList(shipOrderItemService.list(new LambdaQueryWrapper<OOrderStockingItem>()
                        .eq(OOrderStockingItem::getShipOrderId, order.getId())));
            }
        }

        return PageResult.build(pages);
    }
}




