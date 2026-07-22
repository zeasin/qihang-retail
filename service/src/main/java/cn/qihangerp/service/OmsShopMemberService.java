package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.OmsShopMember;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 会员服务接口
 */
public interface OmsShopMemberService extends IService<OmsShopMember> {

    /**
     * 根据手机号查询会员
     */
    OmsShopMember queryByPhone(String phone, Long shopId);

    /**
     * 检查手机号是否已存在
     */
    boolean existsByPhone(String phone, Long merchantId);

    /**
     * 分页查询会员列表
     */
    PageResult<OmsShopMember> queryPageList(OmsShopMember query, PageQuery pageQuery);
}
