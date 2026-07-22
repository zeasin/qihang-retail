package cn.qihangerp.service.impl;

import cn.qihangerp.common.ResultVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qihangerp.model.entity.ErpWarehouseShipper;
import cn.qihangerp.service.ErpWarehouseShipperService;
import cn.qihangerp.mapper.ErpWarehouseShipperMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
* @author qilip
* @description 针对表【erp_cloud_warehouse_shipper(云仓承运商)】的数据库操作Service实现
* @createDate 2025-07-16 10:33:47
*/
@Service
public class ErpWarehouseShipperServiceImpl extends ServiceImpl<ErpWarehouseShipperMapper, ErpWarehouseShipper>
    implements ErpWarehouseShipperService {

    @Override
    public ResultVo saveErpCloudWarehouseShop(List<ErpWarehouseShipper> shippers) {
        for (ErpWarehouseShipper shipper : shippers) {
            List<ErpWarehouseShipper> erpCloudWarehouses = this.baseMapper.selectList(
                    new LambdaQueryWrapper<ErpWarehouseShipper>()
                            .eq(ErpWarehouseShipper::getShipperNo, shipper.getShipperNo())
                            .eq(ErpWarehouseShipper::getMerchantId, shipper.getMerchantId())
            );
            if (erpCloudWarehouses != null && erpCloudWarehouses.size() > 0) {
                shipper.setId(erpCloudWarehouses.get(0).getId());
                this.baseMapper.updateById(shipper);
            } else {
                this.baseMapper.insert(shipper);
            }
        }

        List<Long> ids = shippers.stream().map(ErpWarehouseShipper::getId).toList();
        this.baseMapper.delete(new LambdaQueryWrapper<ErpWarehouseShipper>()
                .eq(ErpWarehouseShipper::getMerchantId, shippers.get(0).getMerchantId())
                .notIn(ErpWarehouseShipper::getId,ids)
        );
        return ResultVo.success();
    }

    @Override
    public List<ErpWarehouseShipper> getJdWarehouseShipperList(Long warehouseId,Long merchantId,String ownerNo) {
        List<ErpWarehouseShipper> erpCloudWarehouses = this.baseMapper.selectList(new LambdaQueryWrapper<ErpWarehouseShipper>()
                .eq(merchantId!=null, ErpWarehouseShipper::getMerchantId, merchantId)
                .eq(warehouseId!=null, ErpWarehouseShipper::getWarehouseId, warehouseId)
                .eq(StringUtils.hasText(ownerNo),ErpWarehouseShipper::getOwnerNo, ownerNo)
        );
        return erpCloudWarehouses;
    }
}




