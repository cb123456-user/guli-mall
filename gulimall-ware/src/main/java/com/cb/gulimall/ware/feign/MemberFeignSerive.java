package com.cb.gulimall.ware.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @authoer: chenbin
 * @date: 2022/11/13/013 22:21
 * @description:
 */
@FeignClient("gulimall-member")
public interface MemberFeignSerive {

    @RequestMapping("/member/memberreceiveaddress/info/{id}")
    R AddressInfo(@PathVariable("id") Long id);
}
