package cn.qihangerp.service.impl;

import cn.qihangerp.common.DateHelper;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.mapper.*;
import cn.qihangerp.model.entity.*;
import cn.qihangerp.model.request.PurchaseOrderStockInBo;
import cn.qihangerp.model.request.SearchRequest;
import cn.qihangerp.service.ErpPurchaseOrderShipService;

import cn.qihangerp.utils.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


import java.util.List;

/**
* @author qilip
* @description 针对表【scm_purchase_order_ship(采购订单物流表)】的数据库操作Service实现
* @createDate 2024-10-20 17:18:53
*/
@AllArgsConstructor
@Service
public class ErpPurchaseOrderShipServiceImpl extends ServiceImpl<ErpPurchaseOrderShipMapper, ErpPurchaseOrderShip>
    implements ErpPurchaseOrderShipService {
    private final ErpPurchaseOrderShipMapper shipMapper;
    private final ErpPurchaseOrderMapper orderMapper;
    private final ErpPurchaseOrderItemMapper orderItemMapper;
    private final ErpWarehouseMapper warehouseMapper;
    private final ErpStockInMapper stockInMapper;
    private final ErpStockInItemMapper stockInItemMapper;


    @Override
    public PageResult<ErpPurchaseOrderShip> queryPageList(SearchRequest bo, PageQuery pageQuery) {
        if(org.springframework.util.StringUtils.hasText(bo.getStartTime())){

            boolean b = DateHelper.isValidDate(bo.getStartTime());
            if(!b){
//                bo.setStartTime(bo.getStartTime()+" 00:00:00");
                bo.setStartTime("");
            }
        }
        if(org.springframework.util.StringUtils.hasText(bo.getEndTime())){
            boolean b =DateHelper.isValidDate(bo.getEndTime());
            if(!b){
//                bo.setEndTime(bo.getEndTime()+" 23:59:59");
                bo.setEndTime("");
            }
        }else{
            bo.setEndTime(bo.getShipTime());
        }

        LambdaQueryWrapper<ErpPurchaseOrderShip> queryWrapper = new LambdaQueryWrapper<ErpPurchaseOrderShip>()
                .eq(bo.getMerchantId()!=null, ErpPurchaseOrderShip::getMerchantId,bo.getMerchantId())
                .eq(bo.getShopId()!=null, ErpPurchaseOrderShip::getShopId,bo.getShopId())
                .eq(bo.getSupplierId()!=null, ErpPurchaseOrderShip::getSupplierId,bo.getSupplierId())
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderNum()), ErpPurchaseOrderShip::getOrderNum,bo.getOrderNum())
                .eq(bo.getOrderStatus()!=null, ErpPurchaseOrderShip::getStatus,bo.getOrderStatus())
                .ge(org.springframework.util.StringUtils.hasText(bo.getStartTime()), ErpPurchaseOrderShip::getShipTime,bo.getStartTime()+" 00:00:00")
                .le(org.springframework.util.StringUtils.hasText(bo.getEndTime()), ErpPurchaseOrderShip::getShipTime,bo.getEndTime()+" 23:59:59")
                ;
        if(org.springframework.util.StringUtils.hasText(bo.getShipTime())){
            boolean b = DateHelper.isValidDate(bo.getShipTime());
            if(b){
                queryWrapper.ge( ErpPurchaseOrderShip::getShipTime,bo.getShipTime()+" 00:00:00")
                        .le( ErpPurchaseOrderShip::getShipTime,bo.getShipTime()+" 23:59:59");
            }
        }

        Page<ErpPurchaseOrderShip> pages = shipMapper.selectPage(pageQuery.build(), queryWrapper);

        return PageResult.build(pages);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updateScmPurchaseOrderShip(ErpPurchaseOrderShip erpPurchaseOrderShip)
    {
        ErpPurchaseOrderShip ship = shipMapper.selectById(erpPurchaseOrderShip.getId());
        if(ship== null) return -1;
        else if(ship.getStatus()!=0)return -2;
        // 更新采购单状态
        ErpPurchaseOrder order = new ErpPurchaseOrder();
        order.setId(erpPurchaseOrderShip.getOrderId());
        order.setStatus(2);
        order.setReceivedTime(erpPurchaseOrderShip.getReceiptTime());
        order.setUpdateTime(DateUtils.getNowDate());
        order.setUpdateBy(erpPurchaseOrderShip.getUpdateBy());
        orderMapper.updateById(order);
        //更新
        ErpPurchaseOrderShip update = new ErpPurchaseOrderShip();
        update.setId(ship.getId());
        update.setUpdateTime(DateUtils.getNowDate());
        update.setUpdateBy(erpPurchaseOrderShip.getUpdateBy());
        update.setStatus(1);
        update.setRemark(erpPurchaseOrderShip.getRemark());
        update.setReceiptTime(erpPurchaseOrderShip.getReceiptTime());
//        update.setReceiptTime(DateUtils.getNowDate());
        update.setId(erpPurchaseOrderShip.getId());
        return shipMapper.updateById(update);
    }

    @Transactional
    @Override
    public ResultVo<Long> createStockInEntry(PurchaseOrderStockInBo bo,Long userId,String userName) {
        if(bo.getId()==null) return ResultVo.error("缺少参数：ID");
        ErpPurchaseOrderShip ship = shipMapper.selectById(bo.getId());
        if (ship == null) return ResultVo.error("采购物流不存在");
        else if (ship.getStatus().intValue() == 2) return ResultVo.error("已处理过了请勿重复操作");

        // 自动查找本地仓，找不到用默认值
        ErpWarehouse warehouse = warehouseMapper.selectOne(
            new LambdaQueryWrapper<ErpWarehouse>().eq(ErpWarehouse::getWarehouseType, "LOCAL").last("limit 1"));
        if (warehouse == null) {
            warehouse = new ErpWarehouse();
            warehouse.setId(0L);
            warehouse.setWarehouseName("本地仓");
            warehouse.setWarehouseType("LOCAL");
        }
        // 子表（使用采购单item数据）
        List<ErpPurchaseOrderItem> purchaseOrderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, ship.getOrderId()));
        if(purchaseOrderItems.isEmpty()){
            return ResultVo.error("没有找打采购明细");
        }
        // 更新采购物流表状态
        ErpPurchaseOrderShip update = new ErpPurchaseOrderShip();
        update.setUpdateTime(DateUtils.getNowDate());
        update.setStockInTime(DateUtils.getNowDate());
        if (bo.getReceiptTime() != null && !bo.getReceiptTime().isEmpty()) {
            update.setReceiptTime(java.time.LocalDateTime.of(
                java.time.LocalDate.parse(bo.getReceiptTime(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                java.time.LocalTime.MIN));
        } else {
            update.setReceiptTime(DateUtils.getNowDate());
        }
        update.setRemark(bo.getRemark());
        update.setUpdateBy(bo.getCreateBy());
        update.setWarehouseId(warehouse.getId());
        update.setWarehouseType(warehouse.getWarehouseType());
        update.setWarehouseName(warehouse.getWarehouseName());
        update.setStatus(2);
        update.setId(ship.getId());
        shipMapper.updateById(update);

        ErpPurchaseOrder erpPurchaseOrder = orderMapper.selectById(ship.getOrderId());
        if(erpPurchaseOrder==null) return ResultVo.error("采购单不存在");

        // 更新采购订单状态
        ErpPurchaseOrder order = new ErpPurchaseOrder();
        order.setId(ship.getOrderId());
        order.setStatus(3);
        if (bo.getReceiptTime() != null && !bo.getReceiptTime().isEmpty()) {
            order.setReceivedTime(java.time.LocalDateTime.of(
                java.time.LocalDate.parse(bo.getReceiptTime(), java.time.format.DateTimeFormatter.ISO_LOCAL_DATE),
                java.time.LocalTime.MIN));
        } else {
            order.setReceivedTime(DateUtils.getNowDate());
        }
        order.setWarehouseId(warehouse.getId());
        order.setWarehouseType(warehouse.getWarehouseType());
        order.setWarehouseName(warehouse.getWarehouseName());
        order.setStockInTime(DateUtils.getNowDate());
        order.setUpdateTime(DateUtils.getNowDate());
        order.setUpdateBy("生成入库单");
        orderMapper.updateById(order);

        // 创建入库单
        ErpStockIn stockIn = new ErpStockIn();
        stockIn.setStockInNum("SI" + ship.getOrderNum());
        stockIn.setStockInType(1);
        stockIn.setSourceNo(ship.getOrderNum());
        stockIn.setSourceId(ship.getOrderId());
        stockIn.setSourceGoodsUnit(purchaseOrderItems.size());
        int totalQty = purchaseOrderItems.stream().mapToInt(e -> e.getQuantity().intValue()).sum();
        stockIn.setSourceSpecUnitTotal(totalQty);
        stockIn.setSourceSpecUnit(purchaseOrderItems.size());
        stockIn.setWarehouseId(warehouse.getId());
        stockIn.setWarehouseName(warehouse.getWarehouseName());
        stockIn.setWarehouseType(warehouse.getWarehouseType());
        stockIn.setStatus(0);
        stockIn.setCreateBy(userName);
        stockIn.setCreateTime(DateUtils.getNowDate());
        stockIn.setMerchantId(erpPurchaseOrder.getMerchantId());
        stockIn.setShopId(erpPurchaseOrder.getShopId());
        stockInMapper.insert(stockIn);

        // 创建入库单明细
        for (ErpPurchaseOrderItem item : purchaseOrderItems) {
            ErpStockInItem stockInItem = new ErpStockInItem();
            stockInItem.setStockInId(stockIn.getId());
            stockInItem.setStockInType(1);
            stockInItem.setSourceNo(ship.getOrderNum());
            stockInItem.setSourceId(ship.getOrderId());
            stockInItem.setSourceItemId(Long.parseLong(item.getId()));
            stockInItem.setGoodsId(item.getGoodsId() != null ? String.valueOf(item.getGoodsId()) : null);
            stockInItem.setGoodsNum(item.getGoodsNum());
            stockInItem.setGoodsName(item.getGoodsName());
            stockInItem.setGoodsImage(item.getColorImage());
            stockInItem.setSkuId(item.getId());
            stockInItem.setSkuCode(item.getSpecNum());
            stockInItem.setSkuName(buildSkuName(item));
            stockInItem.setQuantity(item.getQuantity() != null ? item.getQuantity().intValue() : 0);
            stockInItem.setInQuantity(0);
            stockInItem.setInventoryMode(item.getInventoryMode());
            stockInItem.setPurPrice(item.getPrice() != null ? item.getPrice().doubleValue() : 0);
            stockInItem.setWarehouseId(warehouse.getId());
            stockInItem.setStatus(0);
            stockInItem.setCreateBy(userName);
            stockInItem.setCreateTime(DateUtils.getNowDate());
            stockInItem.setMerchantId(erpPurchaseOrder.getMerchantId());
            stockInItem.setShopId(erpPurchaseOrder.getShopId());
            stockInItemMapper.insert(stockInItem);
        }

        return ResultVo.success(ship.getId());
    }

    private String buildSkuName(ErpPurchaseOrderItem item) {
        String skuName = "";
        if (StringUtils.hasText(item.getColorValue())) {
            skuName += item.getColorValue();
        }
        if (StringUtils.hasText(item.getSizeValue())) {
            skuName += " " + item.getSizeValue();
        }
        if (StringUtils.hasText(item.getStyleValue())) {
            skuName += " " + item.getStyleValue();
        }
        return skuName;
    }
}




