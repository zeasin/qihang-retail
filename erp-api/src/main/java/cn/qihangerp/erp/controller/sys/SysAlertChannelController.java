package cn.qihangerp.erp.controller.sys;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.SysAlertChannel;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.ISysAlertChannelService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;

@AllArgsConstructor
@RestController
@RequestMapping("/api/sys-api/alert/channel")
public class SysAlertChannelController extends BaseController {
    private final ISysAlertChannelService channelService;

    @GetMapping("/list")
    public TableDataInfo list(SysAlertChannel bo, PageQuery pageQuery) {
        LambdaQueryWrapper<SysAlertChannel> w = new LambdaQueryWrapper<>();
        w.orderByDesc(SysAlertChannel::getCreateTime);
        Page<SysAlertChannel> page = channelService.page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), w);
        return getDataTable(PageResult.build(page));
    }

    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(channelService.getById(id));
    }

    @PostMapping
    public AjaxResult add(@RequestBody SysAlertChannel entity) {
        entity.setCreateTime(LocalDateTime.now());
        return toAjax(channelService.save(entity));
    }

    @PutMapping
    public AjaxResult edit(@RequestBody SysAlertChannel entity) {
        entity.setUpdateTime(LocalDateTime.now());
        return toAjax(channelService.updateById(entity));
    }

    @PostMapping("/test")
    public AjaxResult test() {
        return error("升级中");
    }

    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(channelService.removeByIds(Arrays.asList(ids)));
    }
}
