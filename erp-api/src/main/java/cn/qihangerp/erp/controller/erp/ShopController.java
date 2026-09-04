package cn.qihangerp.erp.controller.erp;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.OShop;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OShopService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 店铺管理Controller（即时零售平台店铺）
 * 复用 o_shop 表，type 对应各电商平台ID
 */
@AllArgsConstructor
@RestController
@RequestMapping("/erp-api/shop")
public class ShopController extends BaseController {

    private final OShopService shopService;

    /**
     * 查询店铺列表
     */
    @GetMapping("/list")
    public TableDataInfo list(OShop query, PageQuery pageQuery) {
        PageResult<OShop> pageList = shopService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 获取店铺详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(shopService.selectShopById(id));
    }

    /**
     * 新增店铺
     */
    @PostMapping
    public AjaxResult add(@RequestBody OShop shop) {
        return toAjax(shopService.insertShop(shop));
    }

    /**
     * 修改店铺
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OShop shop) {
        return toAjax(shopService.updateShop(shop));
    }

    /**
     * 删除店铺
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(shopService.deleteShopByIds(ids));
    }

    /**
     * 获取店铺选项（下拉选择用）
     */
    @GetMapping("/options")
    public AjaxResult options(OShop query) {
        List<OShop> list = shopService.queryList(query);
        return success(list);
    }
}
