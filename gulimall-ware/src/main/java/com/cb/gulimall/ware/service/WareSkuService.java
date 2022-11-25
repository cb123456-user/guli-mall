package com.cb.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.ware.entity.WareSkuEntity;
import com.cb.gulimall.ware.vo.SkuHasStockVo;
import com.cb.gulimall.ware.vo.SkuStockLockVo;
import com.cb.gulimall.ware.vo.StockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:28:06
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void addStock(StockVo stockVo);

    List<SkuHasStockVo> hasStock(List<Long> skuIdList);

    void skuStockLock(SkuStockLockVo skuStockLockVo);
}

