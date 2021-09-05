package com.cb.gulimall.member.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-coupon")
public interface CouponService {

    @GetMapping("coupon/coupon/member/coupon/test")
    public R test();
}
