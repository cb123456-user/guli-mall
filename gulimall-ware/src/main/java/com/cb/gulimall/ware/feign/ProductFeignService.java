package com.cb.gulimall.ware.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient("gulimall-product")
public interface ProductFeignService {
    /**
     *  远程调用 product 获取商品名称
     *
     *   1)、让所有请求过网关；
     *          1、@FeignClient("mall-gateway")：给mall-gateway所在的机器发请求
     *          2、/api/product/skuinfo/info/{skuId}
     *   2）、直接让后台指定服务处理
     *          1、@FeignClient(gulimall-product")
     *          2、/product/skuinfo/info/{skuId}
     */
    @RequestMapping("/product/skuinfo/info/{skuId}")
    public R info(@PathVariable("skuId") Long skuId);
}
