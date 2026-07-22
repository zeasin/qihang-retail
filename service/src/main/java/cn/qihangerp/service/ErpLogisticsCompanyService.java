package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpLogisticsCompany;
import cn.qihangerp.request.LogisticsCompanyRequest;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author TW
* @description 针对表【sys_logistics_company(平台快递公司表)】的数据库操作Service
* @createDate 2024-03-22 11:03:11
*/
public interface ErpLogisticsCompanyService extends IService<ErpLogisticsCompany> {
    PageResult<ErpLogisticsCompany> queryPageList(LogisticsCompanyRequest request, PageQuery pageQuery);
    List<ErpLogisticsCompany> queryListByStatus(String name, Integer status, Integer shopType, Integer shopId, Long supplierId);
    int updateStatus(String id,Integer status);
    int insert(ErpLogisticsCompany logisticsCompany);
    ErpLogisticsCompany queryByCode(String code,Integer shopType);
    
    /**
     * 按名称去重查询快递公司（用于常用快递公司选择）
     */
    List<ErpLogisticsCompany> queryDistinctByName(String name);
    
    /**
     * 根据名称和平台ID查询快递公司
     */
    ErpLogisticsCompany queryByNameAndPlatform(String name, Integer platformId);
}
