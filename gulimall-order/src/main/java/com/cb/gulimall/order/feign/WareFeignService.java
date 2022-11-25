package com.cb.gulimall.order.feign;

import com.cb.common.utils.R;
import com.cb.gulimall.order.vo.SkuStockLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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

    @GetMapping("/ware/wareinfo/fare")
    public R fare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/sku/stock/lock")
    public R skuStockLock(@RequestBody SkuStockLockVo skuStockLockVo);
}
