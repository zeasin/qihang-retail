package cn.qihangerp.service.impl;

import cn.qihangerp.model.entity.ErpStockOutItem;
import cn.qihangerp.mapper.ErpStockOutItemMapper;
import cn.qihangerp.service.ErpStockOutItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
* @author qilip
* @description 针对表【wms_stock_out_item(出库单明细)】的数据库操作Service实现
* @createDate 2024-09-22 11:13:23
*/
@AllArgsConstructor
@Service
public class ErpStockOutItemServiceImpl extends ServiceImpl<ErpStockOutItemMapper, ErpStockOutItem>
    implements ErpStockOutItemService {
}




