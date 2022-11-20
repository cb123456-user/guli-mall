package com.cb.gulimall.auth.feign;

import com.cb.common.utils.R;
import com.cb.common.vo.MemberLoginVo;
import com.cb.common.vo.MemberRegistVo;
import com.cb.common.vo.SocialUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("/member/member/resist")
    public R resist(@RequestBody MemberRegistVo vo);

    @PostMapping("/member/member/login")
    public R login(@RequestBody MemberLoginVo vo);

    @PostMapping("/member/member/oauth2/login")
    public R oauth2Login(@RequestBody SocialUser socialUser);
}
