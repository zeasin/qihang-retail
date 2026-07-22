package cn.qihangerp.erp.controller.pos;

import cn.qihangerp.common.AjaxResult;
import cn.qihangerp.common.PageQuery;
import cn.qihangerp.common.PageResult;
import cn.qihangerp.common.TableDataInfo;
import cn.qihangerp.model.entity.OmsShopMember;
import cn.qihangerp.security.common.BaseController;
import cn.qihangerp.service.OmsShopMemberService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 会员查询Controller（POS收银用）
 * 复用 oms_shop_member 表
 */
@AllArgsConstructor
@RestController
@RequestMapping("/api/pos-api/member")
public class PosMemberController extends BaseController {

    private final OmsShopMemberService memberService;

    /**
     * 按手机号查询会员（收银台输入手机号）
     */
    @GetMapping("/phone/{phone}")
    public AjaxResult getByPhone(@PathVariable String phone, @RequestParam Long shopId) {
        OmsShopMember member = memberService.queryByPhone(phone, shopId);
        if (member == null) {
            return AjaxResult.error("未找到该手机号对应的会员");
        }
        return success(member);
    }

    /**
     * 按会员ID查询
     */
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        OmsShopMember member = memberService.getById(id);
        if (member == null) {
            return AjaxResult.error("会员不存在");
        }
        return success(member);
    }

    /**
     * 新增会员（收银时快速注册）
     */
    @PostMapping
    public AjaxResult add(@RequestBody OmsShopMember member) {
        // 检查手机号是否已存在
        if (memberService.existsByPhone(member.getPhone(), member.getMerchantId())) {
            return AjaxResult.error("该手机号已注册");
        }
        return toAjax(memberService.save(member));
    }

    /**
     * 修改会员信息
     */
    @PutMapping
    public AjaxResult edit(@RequestBody OmsShopMember member) {
        return toAjax(memberService.updateById(member));
    }

    /**
     * 查询会员列表
     */
    @GetMapping("/list")
    public TableDataInfo list(OmsShopMember query, PageQuery pageQuery) {
        PageResult<OmsShopMember> pageList = memberService.queryPageList(query, pageQuery);
        return getDataTable(pageList);
    }
}
