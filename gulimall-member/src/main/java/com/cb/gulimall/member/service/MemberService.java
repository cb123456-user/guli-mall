package com.cb.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.common.vo.MemberLoginVo;
import com.cb.common.vo.MemberRegistVo;
import com.cb.common.vo.SocialUser;
import com.cb.gulimall.member.entity.MemberEntity;

import java.util.Map;

/**
 * 会员
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:18:01
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void resist(MemberRegistVo vo);

    MemberEntity login(MemberLoginVo vo);

    MemberEntity login(SocialUser socialUser);
}

