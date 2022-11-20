package com.cb.gulimall.product.dao;

import com.cb.gulimall.product.entity.AttrEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cb.gulimall.product.vo.SpuGroupAttrVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品属性
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@Mapper
public interface AttrDao extends BaseMapper<AttrEntity> {

    List<SpuGroupAttrVo> getSpuGroupAttrs(@Param("spuId") Long spuId, @Param("catalogId") Long catalogId);
}
