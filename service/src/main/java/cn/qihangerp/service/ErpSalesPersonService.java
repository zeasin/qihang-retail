package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpSalesPerson;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 营业员服务接口
 */
public interface ErpSalesPersonService extends IService<ErpSalesPerson> {

    /**
     * 分页查询营业员列表
     */
    PageResult<ErpSalesPerson> queryPageList(ErpSalesPerson query, PageQuery pageQuery);

    /**
     * 查询营业员列表
     */
    List<ErpSalesPerson> queryList(ErpSalesPerson query);
}
