package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.OGoods;
import cn.qihangerp.model.entity.OGoodsSku;
import cn.qihangerp.model.query.GoodsSkuQuery;
import cn.qihangerp.model.vo.GoodsSpecListVo;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OGoodsService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品查询Controller（POS收银用）
 * 复用 o_goods + o_goods_sku 表
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/goods")
public class PosGoodsController extends BaseController {

    private final OGoodsService goodsService;

    /**
     * 按条码查询商品（扫码枪扫码）
     * 通过搜索接口实现，关键词为条码
     */
    @GetMapping("/barcode/{barcode}")
    public AjaxResult getByBarcode(@PathVariable String barcode) {
        List<GoodsSpecListVo> list = goodsService.searchGoodsSpec(barcode);
        if (list == null || list.isEmpty()) {
            return AjaxResult.error("未找到该条码对应的商品");
        }
        return success(list);
    }

    /**
     * 按关键词搜索商品（收银台输入搜索）
     */
    @GetMapping("/search")
    public TableDataInfo search(String keyword) {
        List<GoodsSpecListVo> list = goodsService.searchGoodsSpec(keyword);
        return getDataTable(list);
    }

    /**
     * 查询商品详情（含SKU列表）
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        OGoods goods = goodsService.selectGoodsById(id);
        if (goods == null) {
            return AjaxResult.error("商品不存在");
        }
        return success(goods);
    }

    /**
     * 查询商品的SKU列表
     */
    @GetMapping("/{goodsId}/sku")
    public AjaxResult getSkuList(@PathVariable("goodsId") Long goodsId) {
        List<OGoodsSku> skuList = goodsService.querySkuByGoodsId(goodsId);
        return success(skuList);
    }
}
