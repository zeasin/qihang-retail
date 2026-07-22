package cn.qihangerp.service;

import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpWarehouseShipper;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author qilip
* @description 针对表【erp_cloud_warehouse_shipper(云仓承运商)】的数据库操作Service
* @createDate 2025-07-16 10:33:47
*/
public interface ErpWarehouseShipperService extends IService<ErpWarehouseShipper> {
    ResultVo saveErpCloudWarehouseShop(List<ErpWarehouseShipper> shippers);
    List<ErpWarehouseShipper> getJdWarehouseShipperList(Long warehouseId,Long merchantId,String ownerNo);
}
