package com.cb.gulimall.product.dao;

import com.cb.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cb.gulimall.product.vo.SkuAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * sku销售属性&值
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@Mapper
public interface SkuSaleAttrValueDao extends BaseMapper<SkuSaleAttrValueEntity> {

    List<SkuAttrVo> selectSkuAttrBySpuId(@Param("spuId") Long spuId);

    List<String> getSkuSaleAttrValuesBySkuId(@Param("skuId") Long skuId);

}
