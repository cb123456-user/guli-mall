package com.cb.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.cb.gulimall.member.feign.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cb.gulimall.member.entity.MemberEntity;
import com.cb.gulimall.member.service.MemberService;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.R;


/**
 * 会员
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:18:01
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponService couponService;

    @GetMapping("/test")
    public R test() {
        MemberEntity member = new MemberEntity();
        member.setUsername("test-name");

        R couponResult = couponService.test();

        return R.ok()
                .put("member", member)
                .put("coupons", couponResult.get("coupons"));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("member:member:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("member:member:info")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("member:member:save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("member:member:update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("member:member:delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
