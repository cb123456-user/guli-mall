package com.cb.gulimall.order.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author chenbin
 * @date 2022/11/24 20:44
 */
@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/select/skuId")
    R selectSpuInfo(@RequestParam("skuId") Long skuId);
}
