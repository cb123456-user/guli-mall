package com.cb.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.cb.common.exception.BzipCodeEnum;
import com.cb.common.vo.MemberLoginVo;
import com.cb.common.vo.MemberRegistVo;
import com.cb.common.vo.SocialUser;
import com.cb.gulimall.member.exceptiion.PhoneExistException;
import com.cb.gulimall.member.exceptiion.UserNameExistException;
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

    /**
     * 社交登录
     * @param socialUser
     * @return
     */
    @PostMapping("/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser) {
        MemberEntity memberEntity = memberService.login(socialUser);
        if (memberEntity == null) {
            return R.error(BzipCodeEnum.LOGIN_VALID_EXCEPTION.getCode(), BzipCodeEnum.VALID_FAILD_EXCEPTION.getMsg());
        }

        return R.ok().setData(memberEntity);
    }

    @PostMapping("/login")
    public R login(@RequestBody MemberLoginVo vo) {
        MemberEntity memberEntity = memberService.login(vo);
        if (memberEntity == null) {
            return R.error(BzipCodeEnum.LOGIN_VALID_EXCEPTION.getCode(), BzipCodeEnum.VALID_FAILD_EXCEPTION.getMsg());
        }

        return R.ok().setData(memberEntity);
    }

    @PostMapping("/resist")
    public R resist(@RequestBody MemberRegistVo vo) {
        try {
            memberService.resist(vo);
        } catch (UserNameExistException e) {
            return R.error(BzipCodeEnum.USER_EXIST_EXCEPTION.getCode(), BzipCodeEnum.USER_EXIST_EXCEPTION.getMsg());
        } catch (PhoneExistException e) {
            return R.error(BzipCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BzipCodeEnum.PHONE_EXIST_EXCEPTION.getMsg());
        }

        return R.ok();
    }

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
