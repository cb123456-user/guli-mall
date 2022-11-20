package com.cb.gulimall.order.feign;

import com.cb.gulimall.order.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @authoer: chenbin
 * @date: 2022/11/13/013 14:18
 * @description:
 */
@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/cartItems")
    public List<OrderItemVo> memberCartItems();
}
