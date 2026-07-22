package cn.qihangerp.service.impl;

import cn.qihangerp.model.entity.ErpPurchaseLogistics;
import cn.qihangerp.mapper.ErpPurchaseLogisticsMapper;
import cn.qihangerp.service.ErpPurchaseLogisticsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.stereotype.Service;

/**
* @author qilip
* @description 针对表【scm_logistics(采购物流公司表)】的数据库操作Service实现
* @createDate 2024-12-08 18:17:15
*/
@Service
public class ErpPurchaseLogisticsServiceImpl extends ServiceImpl<ErpPurchaseLogisticsMapper, ErpPurchaseLogistics>
    implements ErpPurchaseLogisticsService {

    @Override
    public int updateStatus(Long id, Integer status) {
        ErpPurchaseLogistics update = new ErpPurchaseLogistics();
        update.setId(id);
        update.setStatus(status);
        return this.baseMapper.updateById(update);
    }
}




