package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.model.entity.ErpSupplierProductItem;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ErpSupplierProductItemService extends IService<ErpSupplierProductItem> {

    PageResult<ErpSupplierProductItem> queryPageList(ErpSupplierProductItem item, PageQuery pageQuery);
}
