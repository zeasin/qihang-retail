package cn.qihangerp.service.impl;

import cn.qihangerp.common.DateHelper;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpPurchaseOrder;
import cn.qihangerp.model.entity.ErpPurchaseOrderItem;
import cn.qihangerp.model.entity.ErpPurchaseOrderShip;
import cn.qihangerp.model.entity.OGoodsSku;
import cn.qihangerp.model.request.PurchaseOrderAddItemBo;
import cn.qihangerp.model.request.PurchaseOrderAddRequest;
import cn.qihangerp.model.request.PurchaseOrderOptionRequest;
import cn.qihangerp.model.request.SearchRequest;
import cn.qihangerp.mapper.ErpPurchaseOrderItemMapper;
import cn.qihangerp.mapper.ErpPurchaseOrderMapper;
import cn.qihangerp.mapper.ErpPurchaseOrderShipMapper;
import cn.qihangerp.mapper.OGoodsSkuMapper;
import cn.qihangerp.service.ErpPurchaseOrderService;
import cn.qihangerp.utils.DateUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author qilip
* @description 针对表【scm_purchase_order(采购订单)】的数据库操作Service实现
* @createDate 2024-10-20 15:36:33
*/
@AllArgsConstructor
@Service
public class ErpPurchaseOrderServiceImpl extends ServiceImpl<ErpPurchaseOrderMapper, ErpPurchaseOrder>
    implements ErpPurchaseOrderService {
    private final ErpPurchaseOrderMapper erpPurchaseOrderMapper;
    private final ErpPurchaseOrderItemMapper erpPurchaseOrderItemMapper;
    private final ErpPurchaseOrderShipMapper shipMapper;
    private final OGoodsSkuMapper goodsSkuMapper;

    
    @Override
    public PageResult<ErpPurchaseOrder> queryPageList(SearchRequest bo, PageQuery pageQuery) {
        if(org.springframework.util.StringUtils.hasText(bo.getStartTime())){
            boolean b = DateHelper.isValidDate(bo.getStartTime());
            if(!b){
//                bo.setStartTime(bo.getStartTime()+" 00:00:00");
                bo.setStartTime("");
            }
        }
        if(org.springframework.util.StringUtils.hasText(bo.getEndTime())){
            boolean b = DateHelper.isValidDate(bo.getEndTime());
            if(!b){
//                bo.setEndTime(bo.getEndTime()+" 23:59:59");
                bo.setEndTime("");
            }
        }else {
            bo.setEndTime(bo.getStartTime());
        }

        LambdaQueryWrapper<ErpPurchaseOrder> queryWrapper = new LambdaQueryWrapper<ErpPurchaseOrder>()
                .eq(bo.getMerchantId()!=null, ErpPurchaseOrder::getMerchantId,bo.getMerchantId())
                .eq(bo.getShopId()!=null, ErpPurchaseOrder::getShopId,bo.getShopId())
                .eq(bo.getSupplierId()!=null, ErpPurchaseOrder::getSupplierId,bo.getSupplierId())
                .eq(org.springframework.util.StringUtils.hasText(bo.getOrderNum()), ErpPurchaseOrder::getOrderNum,bo.getOrderNum())
                .eq(bo.getOrderStatus()!=null, ErpPurchaseOrder::getStatus,bo.getOrderStatus())
                .ge(org.springframework.util.StringUtils.hasText(bo.getStartTime()), ErpPurchaseOrder::getOrderTime,bo.getStartTime()+" 00:00:00")
                .le(org.springframework.util.StringUtils.hasText(bo.getEndTime()), ErpPurchaseOrder::getOrderTime,bo.getEndTime()+" 23:59:59")
                ;

        if(org.springframework.util.StringUtils.hasText(bo.getOrderDate())){

            boolean b = DateHelper.isValidDate(bo.getOrderDate());
            if(b){
//                queryWrapper.eq(ErpPurchaseOrder::getOrderDate,bo.getOrderDate());
                queryWrapper.last(" and order_date = '"+bo.getOrderDate()+"'");
            }
        }
        pageQuery.setOrderByColumn("order_time");
        pageQuery.setIsAsc("desc");
        Page<ErpPurchaseOrder> pages = erpPurchaseOrderMapper.selectPage(pageQuery.build(), queryWrapper);

        // 查询子订单
        if(pages.getRecords()!=null){
            for (ErpPurchaseOrder order:pages.getRecords()) {
                order.setItemList(erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, order.getId())));

            }
        }

        return PageResult.build(pages);
    }

    @Override
    public ErpPurchaseOrder getDetailById(Long id) {
        ErpPurchaseOrder order = erpPurchaseOrderMapper.selectById(id);
        if(order!=null){
            order.setItemList(erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, order.getId())));
            return order;
        }else
            return null;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ResultVo<Long> createPurchaseOrder(PurchaseOrderAddRequest addBo) {
        if(addBo.getGoodsList() == null || addBo.getGoodsList().isEmpty()) return ResultVo.error("商品明细不能为空");

        // 添加主表
        ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
        erpPurchaseOrder.setMerchantId(addBo.getMerchantId());
        erpPurchaseOrder.setShopId(addBo.getShopId());
        erpPurchaseOrder.setOrderNum("PUR"+ DateUtils.parseDateToStr("yyyyMMddHHmmss",LocalDateTime.now()));
        erpPurchaseOrder.setOrderAmount(addBo.getOrderAmount());
        erpPurchaseOrder.setCreateTime(DateUtils.getNowDate());
        erpPurchaseOrder.setOrderDate(addBo.getOrderDate());
        erpPurchaseOrder.setSupplierId(addBo.getContactId());
        erpPurchaseOrder.setOrderTime(System.currentTimeMillis()/1000);
        erpPurchaseOrder.setCreateBy(addBo.getCreateBy());
        if(addBo.getStatus() != null){
            if(addBo.getStatus().intValue()==1){
                erpPurchaseOrder.setStatus(1);
                erpPurchaseOrder.setAuditTime(System.currentTimeMillis()/1000);
                erpPurchaseOrder.setAuditUser(addBo.getCreateBy());
            }
        }else{
        erpPurchaseOrder.setStatus(0);
        }
        erpPurchaseOrder.setShipAmount(BigDecimal.ZERO);
//        erpPurchaseOrderMapper.insert(erpPurchaseOrder);
        List<ErpPurchaseOrderItem> items = new ArrayList<>();
        // 添加子表
        for (PurchaseOrderAddItemBo item:addBo.getGoodsList()) {
            OGoodsSku oGoodsSku = goodsSkuMapper.selectById(item.getId());
            if(oGoodsSku == null) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return ResultVo.error("找不到商品信息："+item.getId());
            }
            ErpPurchaseOrderItem orderItem = new ErpPurchaseOrderItem();
            orderItem.setOrderDate(addBo.getOrderDate());
            orderItem.setOrderId(erpPurchaseOrder.getId());
            orderItem.setMerchantId(addBo.getMerchantId());
            orderItem.setShopId(addBo.getShopId());
            orderItem.setOrderNum(erpPurchaseOrder.getOrderNum());
            if(item.getAmount()!=null) {
                orderItem.setAmount(item.getAmount().doubleValue());
            }else{

                orderItem.setAmount(item.getPurPrice()==null?0.0:item.getPurPrice().multiply(BigDecimal.valueOf(item.getQuantity())).doubleValue());
            }
            orderItem.setGoodsId(Long.parseLong(oGoodsSku.getGoodsId()));
            orderItem.setGoodsNum(item.getNumber());
            orderItem.setIsDelete(0);
            orderItem.setPrice(item.getPurPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSpecId(item.getId());
            orderItem.setInventoryMode(oGoodsSku.getInventoryMode());
            orderItem.setSpecNum(item.getSkuCode());
            orderItem.setStatus(0);
            orderItem.setTransType("Purchase");
            orderItem.setGoodsName(item.getGoodsName());
            orderItem.setColorValue(item.getColorValue());
            orderItem.setColorImage(item.getColorImage());
            orderItem.setSizeValue(item.getSizeValue());
            orderItem.setStyleValue(item.getStyleValue());
            items.add(orderItem);
        }

        erpPurchaseOrderMapper.insert(erpPurchaseOrder);
        for(ErpPurchaseOrderItem item:items){
            item.setOrderId(erpPurchaseOrder.getId());
            erpPurchaseOrderItemMapper.insert(item);
        }

        return ResultVo.success(erpPurchaseOrder.getId());
    }

    @Override
    public int updateScmPurchaseOrder(PurchaseOrderOptionRequest request) {
        ErpPurchaseOrder order = erpPurchaseOrderMapper.selectById(request.getId());
        if(order == null) return -1;


        if(request.getOptionType().equals("audit")){
            if(order.getStatus() !=0){
                // 状态不是待审核的
                return -1;
            }
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setAuditUser(request.getAuditUser());
            erpPurchaseOrder.setAuditTime(System.currentTimeMillis()/1000);
            erpPurchaseOrder.setRemark(request.getRemark());
            erpPurchaseOrder.setStatus(1);
            return erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        else if (request.getOptionType().equals("confirm")) {
            if(order.getStatus() !=1){
                // 状态不是已审核的不能确认
                return -1;
            }
            // 查询数据
            List<ErpPurchaseOrderItem> items = erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, request.getId()));

            Map<Long, List<ErpPurchaseOrderItem>> goodsGroup = items.stream().collect(Collectors.groupingBy(x -> x.getGoodsId()));
            Long total = items.stream().mapToLong(ErpPurchaseOrderItem::getQuantity).sum();
            // 生成费用信息
//            ScmPurchaseOrderCost cost = new ScmPurchaseOrderCost();
//            cost.setId(order.getId());
//            cost.setOrderId(order.getId());
//            cost.setSupplierId(order.getSupplierId());
//            cost.setOrderNum(order.getOrderNum());
//            cost.setOrderDate(order.getOrderDate());
//            cost.setOrderGoodsUnit(goodsGroup.size());
//            cost.setOrderSpecUnit(items.size());
//            cost.setOrderSpecUnitTotal(total.intValue());
//            cost.setOrderAmount(order.getOrderAmount());
//            cost.setActualAmount(request.getTotalAmount());
//            cost.setFreight(BigDecimal.ZERO);
//            cost.setConfirmUser(request.getConfirmUser());
//            cost.setConfirmTime(LocalDateTime.now());
//            cost.setCreateBy(request.getUpdateBy());
//            cost.setPayAmount(BigDecimal.ZERO);
//            cost.setPayCount(0);
//            cost.setStatus(0);
//            costMapper.insert(cost);

            // 更新主表
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setStatus(101);
            erpPurchaseOrder.setRemark(request.getRemark());
            erpPurchaseOrder.setSupplierConfirmTime(LocalDateTime.now());
            erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        else if (request.getOptionType().equals("SupplierShip")) {
            if(order.getStatus() !=101 && order.getStatus()!=1){
                // 状态不是已确认的不能发货
                return -1;
            }
            // 查询数据
            List<ErpPurchaseOrderItem> items = erpPurchaseOrderItemMapper.selectList(new LambdaQueryWrapper<ErpPurchaseOrderItem>().eq(ErpPurchaseOrderItem::getOrderId, request.getId()));

            Map<Long, List<ErpPurchaseOrderItem>> goodsGroup = items.stream().collect(Collectors.groupingBy(x -> x.getGoodsId()));
            Long total = items.stream().mapToLong(ErpPurchaseOrderItem::getQuantity).sum();

            // 生成物流信息
            ErpPurchaseOrderShip ship = new ErpPurchaseOrderShip();
            ship.setMerchantId(order.getMerchantId());
            ship.setShopId(order.getShopId());
            ship.setId(order.getId());
            ship.setOrderId(order.getId());
            ship.setSupplierId(order.getSupplierId());
            ship.setOrderNum(order.getOrderNum());
            ship.setOrderDate(order.getOrderDate());
            ship.setOrderGoodsUnit(goodsGroup.size());
            ship.setOrderSpecUnit(items.size());
            ship.setOrderSpecUnitTotal(total.intValue());
            ship.setShipCompany(request.getShipCompany());
            ship.setShipNum(request.getShipNo());
            ship.setFreight(request.getShipCost());
            ship.setShipTime(request.getSupplierDeliveryTime());
            ship.setCreateBy(request.getUpdateBy());
            ship.setRemark(request.getRemark());
            ship.setCreateTime(LocalDateTime.now());
            ship.setStatus(0);
            ship.setBackCount(0);
            ship.setStockInCount(0);
            shipMapper.insert(ship);
            // 更新费用表


//            ScmSupplier scmSupplier = supplierMapper.selectScmSupplierById(order.getContactId());
            // 生成应付信息fms_payable_purchase
//            ScmPurchaseOrderPayable payable = new ScmPurchaseOrderPayable();
//            payable.setSupplierId(order.getSupplierId());
////            fmsPP.setSupplierName(scmSupplier!=null ? scmSupplier.getName():"数据库未找到供应商信息");
//            payable.setAmount(order.getOrderAmount().add(request.getShipCost()));
//            payable.setDate(LocalDateTime.now());
//            payable.setPurchaseOrderNum(order.getOrderNum());
//            payable.setPurchaseDesc("{采购商品总数量:"+total+",不同款式:"+goodsGroup.size()+",不同SKU:"+items.size()+",商品总价:"+order.getOrderAmount()+",运费:"+request.getShipCost()+"}");
//            payable.setStatus(0);
//            payable.setCreateTime(LocalDateTime.now());
//            payable.setCreateBy(request.getUpdateBy());
//            payableMapper.insert(payable);

            // 更新主表
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setStatus(102);
            erpPurchaseOrder.setRemark(request.getRemark());
            erpPurchaseOrder.setSupplierDeliveryTime(LocalDateTime.now());
            erpPurchaseOrder.setShipAmount(request.getShipCost());
            erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        else if (request.getOptionType().equals("cancel")) {
            if(order.getStatus() !=0 && order.getStatus()!=1){
                // 状态不是已确认的不能发货
                return -1;
            }
            ErpPurchaseOrder erpPurchaseOrder = new ErpPurchaseOrder();
            erpPurchaseOrder.setId(order.getId());
            erpPurchaseOrder.setUpdateBy(request.getUpdateBy());
            erpPurchaseOrder.setUpdateTime(DateUtils.getNowDate());
            erpPurchaseOrder.setRemark(request.getRemark());
            erpPurchaseOrder.setStatus(99);
            erpPurchaseOrderMapper.updateById(erpPurchaseOrder);
        }
        return 1;
    }
}




