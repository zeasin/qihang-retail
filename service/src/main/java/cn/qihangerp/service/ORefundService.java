package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ORefund;
import cn.qihangerp.model.request.AfterSaleApplyRequest;
import cn.qihangerp.model.request.AfterSaleAuditRequest;
import cn.qihangerp.model.request.AfterSaleProcessRequest;
import cn.qihangerp.model.request.AfterSaleSearchRequest;
import cn.qihangerp.model.vo.AfterSaleStatsVo;
import cn.qihangerp.model.vo.AfterSaleConfigVo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 售后退款服务接口
 */
public interface ORefundService extends IService<ORefund> {

    /**
     * 分页查询退款列表
     */
    PageResult<ORefund> queryPageList(ORefund query, PageQuery pageQuery);

    /**
     * 售后台账分页查询
     */
    PageResult<ORefund> queryAfterSalePageList(AfterSaleSearchRequest query, PageQuery pageQuery);

    /**
     * 售后详情
     */
    ORefund getAfterSaleDetail(Long id);

    /**
     * 创建售后申请（校验授权期 + 库存可退数量 + 审核阈值）
     */
    ResultVo<Long> createAfterSale(AfterSaleApplyRequest request, String username);

    /**
     * 审核售后（通过/拒绝）
     */
    ResultVo<Long> auditAfterSale(Long id, AfterSaleAuditRequest request, String username);

    /**
     * 退货收货（退货退款/换货类型，收到顾客退回的商品）
     */
    ResultVo<Long> receiveReturnGoods(Long id, String username);

    /**
     * 执行退款（退货退款/仅退款，退款打款 + 库存入库）
     */
    ResultVo<Long> processAfterSale(Long id, AfterSaleProcessRequest request, String username);

    /**
     * 换货发货（换货类型，发送换货商品给顾客）
     */
    ResultVo<Long> shipExchange(Long id, String username);

    /**
     * 取消售后
     */
    ResultVo<Long> cancelAfterSale(Long id, String username);

    /**
     * 台账统计
     */
    AfterSaleStatsVo getAfterSaleStats();

    /**
     * 获取售后配置（授权期、审核阈值、退货原因）
     */
    AfterSaleConfigVo getAfterSaleConfig();
}
