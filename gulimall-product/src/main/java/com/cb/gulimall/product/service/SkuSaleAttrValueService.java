package com.cb.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.cb.gulimall.product.vo.SkuAttrVo;

import java.util.List;
import java.util.Map;

/**
 * sku销售属性&值
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuSaleAttrValues(List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities);

    List<SkuAttrVo> selectSkuAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrValuesBySkuId(Long skuId);
}

