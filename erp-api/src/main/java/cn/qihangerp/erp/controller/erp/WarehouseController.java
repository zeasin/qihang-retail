package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.ErpWarehouse;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ErpWarehouseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/warehouse")
public class WarehouseController extends BaseController {
    private final ErpWarehouseService warehouseService;

    @GetMapping("/list")
    public TableDataInfo list(ErpWarehouse bo)
    {
        LambdaQueryWrapper<ErpWarehouse> qw = new LambdaQueryWrapper<ErpWarehouse>()
                .eq(bo.getStatus()!=null, ErpWarehouse::getStatus, bo.getStatus())
                .like(StringUtils.hasText(bo.getWarehouseNo()), ErpWarehouse::getWarehouseNo,bo.getWarehouseNo())
                .like(StringUtils.hasText(bo.getWarehouseName()), ErpWarehouse::getWarehouseName,bo.getWarehouseName())
                ;
        List<ErpWarehouse> erpWarehouses = warehouseService.list(qw);
        return getDataTable(erpWarehouses);
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(warehouseService.getById(id));
    }
    @PostMapping
    public AjaxResult add(@RequestBody ErpWarehouse warehouse)
    {
        warehouse.setCreateBy(getUsername());
        warehouse.setCreateTime(LocalDateTime.now());
        warehouseService.save(warehouse);
        return AjaxResult.success();
    }
    @PutMapping
    public AjaxResult edit(@RequestBody ErpWarehouse warehouse)
    {
        warehouse.setUpdateBy(getUsername());
        warehouse.setUpdateTime(LocalDateTime.now());
        return toAjax(warehouseService.updateById(warehouse));
    }
	@DeleteMapping("/{ids}")
	public AjaxResult remove(@PathVariable Long[] ids)
	{
	    return toAjax(warehouseService.removeBatchByIds(Arrays.stream(ids).toList()));
	}

	/**
     * 查询云仓列表（用于发货云仓选择，排除本地仓 LOCAL）
     */
    @GetMapping("/cloud_list")
    public TableDataInfo cloudList()
    {
        LambdaQueryWrapper<ErpWarehouse> qw = new LambdaQueryWrapper<ErpWarehouse>()
                .ne(ErpWarehouse::getWarehouseType, "LOCAL")
                .eq(ErpWarehouse::getStatus, 1)
                .orderByDesc(ErpWarehouse::getCreateTime);
        List<ErpWarehouse> list = warehouseService.list(qw);
        return getDataTable(list);
    }

    /**
     * 查询所有可用仓库（仅总部查询）
     */
    @GetMapping("/my_available_list")
    public AjaxResult myAvailableList()
	{
	    LambdaQueryWrapper<ErpWarehouse> qw = new LambdaQueryWrapper<ErpWarehouse>()
	            .eq(ErpWarehouse::getStatus, 1);
	    List<ErpWarehouse> list = warehouseService.list(qw);
	    return success(list);
	}

}
