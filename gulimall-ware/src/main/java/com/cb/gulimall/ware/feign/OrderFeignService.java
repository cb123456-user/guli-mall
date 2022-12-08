package com.cb.gulimall.ware.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @ClassName OrderFeignService
 * @Description
 * @Author JingXu
 * @Date 2022/12/7 20:41
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/ignore/order/status")
    R selectOrderStatus(@RequestParam("orderSn") String orderSn);
}
