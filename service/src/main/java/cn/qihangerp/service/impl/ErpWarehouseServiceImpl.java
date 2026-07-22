package cn.qihangerp.service.impl;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.entity.ErpWarehouse;
import cn.qihangerp.model.request.WarehouseCloudQuery;
import cn.qihangerp.model.request.WarehousePageQuery;
import cn.qihangerp.service.ErpWarehouseService;
import cn.qihangerp.mapper.ErpWarehouseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class ErpWarehouseServiceImpl extends ServiceImpl<ErpWarehouseMapper, ErpWarehouse>
    implements ErpWarehouseService {

    private final ErpWarehouseMapper mapper;

    @Override
    public PageResult<ErpWarehouse> queryPageList(WarehousePageQuery query) {
        LambdaQueryWrapper<ErpWarehouse> qw = new LambdaQueryWrapper<ErpWarehouse>()
            .eq(StringUtils.isNotBlank(query.getWarehouseType()), ErpWarehouse::getWarehouseType, query.getWarehouseType())
            .eq(StringUtils.isNotBlank(query.getWarehouseNo()), ErpWarehouse::getWarehouseNo, query.getWarehouseNo())
            .like(StringUtils.isNotBlank(query.getWarehouseName()), ErpWarehouse::getWarehouseName, query.getWarehouseName())
            .eq(StringUtils.isNotBlank(query.getStatus()), ErpWarehouse::getStatus, query.getStatus())
            .eq(query.getMerchantId() != null, ErpWarehouse::getMerchantId, query.getMerchantId())
            .eq(query.getShopId() != null, ErpWarehouse::getShopId, query.getShopId());
        Page<ErpWarehouse> page = mapper.selectPage(query.build(), qw);
        return PageResult.build(page);
    }

    @Override
    public List<ErpWarehouse> getCloudWarehouseList(WarehouseCloudQuery query) {
        return mapper.selectList(new LambdaQueryWrapper<ErpWarehouse>()
            .eq(ErpWarehouse::getWarehouseType, "CLOUD")
            .eq(StringUtils.isNotBlank(query.getStatus()), ErpWarehouse::getStatus, query.getStatus())
            .eq(query.getMerchantId() != null, ErpWarehouse::getMerchantId, query.getMerchantId()));
    }

    @Override
    public List<ErpWarehouse> getJdWarehouseList(ErpWarehouse bo) {
        return mapper.selectList(new LambdaQueryWrapper<ErpWarehouse>()
            .eq(ErpWarehouse::getWarehouseType, "JDYC")
            .eq(bo.getStatus() != null, ErpWarehouse::getStatus, bo.getStatus()));
    }

    @Override
    public List<ErpWarehouse> getJkyWarehouseList(ErpWarehouse bo) {
        return mapper.selectList(new LambdaQueryWrapper<ErpWarehouse>()
            .eq(ErpWarehouse::getWarehouseType, "CLOUD")
            .eq(bo.getStatus() != null, ErpWarehouse::getStatus, bo.getStatus()));
    }

    @Override
    public List<ErpWarehouse> getWarehouseList(ErpWarehouse bo) {
        return mapper.selectList(new LambdaQueryWrapper<ErpWarehouse>()
            .eq(bo.getWarehouseType() != null, ErpWarehouse::getWarehouseType, bo.getWarehouseType())
            .eq(bo.getStatus() != null, ErpWarehouse::getStatus, bo.getStatus()));
    }

    @Override
    public ResultVo<Long> saveErpCloudWarehouse(List<ErpWarehouse> list) {
        saveBatch(list);
        return ResultVo.success();
    }

    @Override
    public ResultVo shareMerchant(Long warehouseId, Long[] merchantIds) {
        ErpWarehouse warehouse = mapper.selectById(warehouseId);
        if (warehouse == null) return ResultVo.error("仓库不存在");
        String ids = Arrays.stream(merchantIds).map(String::valueOf).collect(Collectors.joining(","));
        warehouse.setMerchantIds(ids);
        mapper.updateById(warehouse);
        return ResultVo.success();
    }

    @Override
    public ErpWarehouse getByLoginName(String loginName) {
        return mapper.selectOne(new LambdaQueryWrapper<ErpWarehouse>()
            .eq(ErpWarehouse::getLoginName, loginName));
    }
}
