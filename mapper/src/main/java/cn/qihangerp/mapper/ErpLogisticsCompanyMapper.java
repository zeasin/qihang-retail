package cn.qihangerp.mapper;

import cn.qihangerp.model.entity.ErpLogisticsCompany;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
* @author TW
* @description 针对表【sys_logistics_company(平台快递公司表)】的数据库操作Mapper
* @createDate 2024-03-22 11:03:11
* @Entity cn.qihangerp.domain.SysLogisticsCompany
*/
public interface ErpLogisticsCompanyMapper extends BaseMapper<ErpLogisticsCompany> {

    /**
     * 按名称去重查询快递公司
     */
    java.util.List<ErpLogisticsCompany> selectDistinctByName(String name);
}




