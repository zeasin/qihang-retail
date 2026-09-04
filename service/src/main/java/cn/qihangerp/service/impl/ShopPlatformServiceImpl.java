package cn.qihangerp.service.impl;

import cn.qihangerp.mapper.OShopPlatformMapper;
import cn.qihangerp.model.entity.OShopPlatform;
import cn.qihangerp.service.IShopPlatformService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 电商平台配置服务实现
 */
@Service
public class ShopPlatformServiceImpl extends ServiceImpl<OShopPlatformMapper, OShopPlatform> implements IShopPlatformService {

    @Override
    public List<OShopPlatform> queryList() {
        LambdaQueryWrapper<OShopPlatform> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(OShopPlatform::getSort);
        return baseMapper.selectList(wrapper);
    }

    @Override
    public OShopPlatform selectPlatformById(Integer id) {
        return baseMapper.selectById(id);
    }

    @Override
    public int insertPlatform(OShopPlatform platform) {
        return baseMapper.insert(platform);
    }

    @Override
    public int updatePlatform(OShopPlatform platform) {
        return baseMapper.updateById(platform);
    }

    @Override
    public int deletePlatformByIds(Integer[] ids) {
        return baseMapper.deleteBatchIds(Arrays.asList(ids));
    }
}
