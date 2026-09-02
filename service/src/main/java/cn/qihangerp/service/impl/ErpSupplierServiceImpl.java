package cn.qihangerp.service.impl;

import cn.qihangerp.model.entity.ErpSupplier;
import cn.qihangerp.mapper.ErpSupplierMapper;
import cn.qihangerp.service.ErpSupplierService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class ErpSupplierServiceImpl extends ServiceImpl<ErpSupplierMapper, ErpSupplier>
    implements ErpSupplierService {
    private final ErpSupplierMapper mapper;

    @Override
    public ErpSupplier getByLoginName(String loginName) {
        LambdaQueryWrapper<ErpSupplier> eq = new LambdaQueryWrapper<ErpSupplier>().eq(ErpSupplier::getLoginName, loginName);
        List<ErpSupplier> ErpSuppliers = mapper.selectList(eq);
        if(ErpSuppliers.isEmpty()) return null;
        else return ErpSuppliers.get(0);
    }

    @Override
    public PageResult<ErpSupplier> queryPageList(ErpSupplier bo, PageQuery pageQuery) {
        LambdaQueryWrapper<ErpSupplier> queryWrapper = new LambdaQueryWrapper<ErpSupplier>();
        queryWrapper.eq(bo.getMerchantId()!=null, ErpSupplier::getMerchantId,bo.getMerchantId());
        queryWrapper.eq(bo.getShopId()!=null, ErpSupplier::getShopId,bo.getShopId());
        queryWrapper.eq(bo.getIsShipper()!=null, ErpSupplier::getIsShipper,bo.getIsShipper());
        queryWrapper.like(StringUtils.hasText(bo.getName()), ErpSupplier::getName, bo.getName());
        queryWrapper.like(StringUtils.hasText(bo.getNumber()), ErpSupplier::getNumber, bo.getNumber());
        queryWrapper.like(StringUtils.hasText(bo.getLinkMan()), ErpSupplier::getLinkMan, bo.getLinkMan());

        Page<ErpSupplier> pages = mapper.selectPage(pageQuery.build(), queryWrapper);

        return PageResult.build(pages);
    }
}
