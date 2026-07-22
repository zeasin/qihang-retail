package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.OShop;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 店铺服务接口
 */
public interface OShopService extends IService<OShop> {

    /**
     * 分页查询店铺列表
     */
    PageResult<OShop> queryPageList(OShop query, PageQuery pageQuery);

    /**
     * 查询店铺列表
     */
    List<OShop> queryList(OShop query);

    /**
     * 根据ID查询店铺
     */
    OShop selectShopById(Long id);

    /**
     * 新增店铺
     */
    int insertShop(OShop shop);

    /**
     * 修改店铺
     */
    int updateShop(OShop shop);

    /**
     * 删除店铺
     */
    int deleteShopByIds(Long[] ids);
}
