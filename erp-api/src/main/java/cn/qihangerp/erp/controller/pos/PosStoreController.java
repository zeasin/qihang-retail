package cn.qihangerp.erp.controller.pos;

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
 * 门店管理Controller（POS收银用）
 * 复用 o_shop 表，type=999 表示线下门店
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/store")
public class PosStoreController extends BaseController {

    private final OShopService shopService;

    /**
     * 查询门店列表
     */
    @GetMapping("/list")
    public TableDataInfo list(OShop query, PageQuery pageQuery) {
        // 固定查询 type=999 的线下门店
        query.setType(999);
        PageResult<OShop> pageList = shopService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }

    /**
     * 获取门店详情
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(shopService.selectShopById(id));
    }

    /**
     * 新增门店
     */
    @PostMapping
    public AjaxResult add(@RequestBody OShop shop) {
        shop.setType(999); // 强制设置为线下门店
        return toAjax(shopService.insertShop(shop));
    }

    /**
     * 修改门店
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OShop shop) {
        return toAjax(shopService.updateShop(shop));
    }

    /**
     * 删除门店
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(shopService.deleteShopByIds(ids));
    }

    /**
     * 获取当前商户的所有线下门店（下拉选择用）
     */
    @GetMapping("/options")
    public AjaxResult options() {
        OShop query = new OShop();
        query.setType(999);
        List<OShop> list = shopService.queryList(query);
        return success(list);
    }
}
