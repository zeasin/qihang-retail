package cn.qihangerp.service;

import cn.qihangerp.model.entity.OShopPlatform;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 电商平台配置服务接口
 */
public interface IShopPlatformService extends IService<OShopPlatform> {

    /**
     * 查询平台列表
     */
    List<OShopPlatform> queryList();

    /**
     * 根据ID查询平台
     */
    OShopPlatform selectPlatformById(Integer id);

    /**
     * 新增平台
     */
    int insertPlatform(OShopPlatform platform);

    /**
     * 修改平台
     */
    int updatePlatform(OShopPlatform platform);

    /**
     * 删除平台
     */
    int deletePlatformByIds(Integer[] ids);
}
