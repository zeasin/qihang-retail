package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ORefund;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 售后退款服务接口
 */
public interface ORefundService extends IService<ORefund> {

    /**
     * 分页查询退款列表
     */
    PageResult<ORefund> queryPageList(ORefund query, PageQuery pageQuery);
}
