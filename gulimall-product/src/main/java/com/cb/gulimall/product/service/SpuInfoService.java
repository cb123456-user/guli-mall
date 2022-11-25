package com.cb.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.product.entity.SpuInfoEntity;
import com.cb.gulimall.product.vo.SpuSaveVo;

import java.util.Map;

/**
 * spu信息
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveApuInfo(SpuSaveVo vo);

    void saveSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryByCondition(Map<String, Object> params);

    void productUp(Long spuId);

    SpuInfoEntity selectSpuInfo(Long skuId);
}

