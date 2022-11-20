package com.cb.gulimall.order.feign;

import com.cb.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @authoer: chenbin
 * @date: 2022/11/13/013 21:24
 * @description:
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/hashstock")
    public R hashstock(@RequestBody List<Long> skuIdList);
}
