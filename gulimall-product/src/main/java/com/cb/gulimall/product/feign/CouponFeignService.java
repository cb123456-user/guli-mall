package com.cb.gulimall.product.feign;

import com.cb.common.to.SkuReductionTo;
import com.cb.common.to.SpuBoundsTo;
import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    /**
     * 保存积分信息
     */
    @PostMapping("/coupon/spubounds/save")
    R save(@RequestBody SpuBoundsTo spuBoundsTo);

    /**
     * 保存优惠信息
     */
    @RequestMapping("/coupon/skufullreduction/saveInfo")
    public R saveInfo(@RequestBody SkuReductionTo skuReductionTo);
}
