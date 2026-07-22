package cn.qihangerp.service;

import cn.qihangerp.model.entity.ErpPurchaseLogistics;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author qilip
* @description 针对表【scm_logistics(采购物流公司表)】的数据库操作Service
* @createDate 2024-12-08 18:17:15
*/
public interface ErpPurchaseLogisticsService extends IService<ErpPurchaseLogistics> {
    int updateStatus(Long id,Integer status);
}
