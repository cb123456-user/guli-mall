package com.cb.gulimall.auth.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-part")
public interface ThirdPartFeignService {

    @GetMapping("/sms/send/code")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
