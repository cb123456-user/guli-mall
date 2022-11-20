package com.cb.gulimall.order.feign;

import com.cb.gulimall.order.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @authoer: chenbin
 * @date: 2022/11/13/013 14:18
 * @description:
 */
@FeignClient("gulimall-member")
public interface MemeberFeignService {

    @GetMapping("/member/memberreceiveaddress/{memberId}/address")
    public List<MemberAddressVo> memberArrress(@PathVariable("memberId") Long memberId);
}
