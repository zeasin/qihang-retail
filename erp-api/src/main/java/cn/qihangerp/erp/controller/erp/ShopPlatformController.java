package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.model.entity.OShopPlatform;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.IShopPlatformService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 电商平台设置Controller
 */
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/shop/platform")
public class ShopPlatformController extends BaseController {

    private final IShopPlatformService platformService;

    /**
     * 查询平台列表
     */
    @GetMapping("/list")
    public AjaxResult list() {
        List<OShopPlatform> list = platformService.queryList();
        return success(list);
    }

    /**
     * 根据ID查询平台
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Integer id) {
        return success(platformService.selectPlatformById(id));
    }

    /**
     * 新增平台
     */
    @PostMapping
    public AjaxResult add(@RequestBody OShopPlatform platform) {
        return toAjax(platformService.insertPlatform(platform));
    }

    /**
     * 修改平台
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OShopPlatform platform) {
        return toAjax(platformService.updatePlatform(platform));
    }

    /**
     * 删除平台
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Integer[] ids) {
        return toAjax(platformService.deletePlatformByIds(ids));
    }
}
