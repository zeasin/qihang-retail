package cn.qihangerp.service;

import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.ResultVo;
import cn.qihangerp.model.bo.SupplierProductAddBo;
import cn.qihangerp.model.bo.SupplierGoodsLinkBo;
import cn.qihangerp.model.entity.ErpSupplierProduct;
import cn.qihangerp.model.entity.ErpSupplierProductItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface ErpSupplierProductService extends IService<ErpSupplierProduct> {

    PageResult<ErpSupplierProduct> queryPageList(ErpSupplierProduct goods, PageQuery pageQuery);

    ResultVo<Long> addProduct(String username, SupplierProductAddBo bo);

    ResultVo updateProduct(String username, SupplierProductAddBo bo);

    void deleteProduct(Long id);

    List<ErpSupplierProductItem> queryItemListByProductId(Long supplierProductId);

    void updateStatus(Long id, Integer status);

    ResultVo linkGoodsFromLibrary(String username, SupplierGoodsLinkBo bo);
}
