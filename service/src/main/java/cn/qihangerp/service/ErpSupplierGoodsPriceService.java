package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpSupplierGoodsPrice;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ErpSupplierGoodsPriceService extends IService<ErpSupplierGoodsPrice> {

    PageResult<ErpSupplierGoodsPrice> queryPageList(ErpSupplierGoodsPrice query, PageQuery pageQuery);
}
