package com.cb.gulimall.product.dao;

import com.cb.gulimall.product.entity.SpuInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * spu信息
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@Mapper
public interface SpuInfoDao extends BaseMapper<SpuInfoEntity> {

    void updateUpStatus(@Param("spuId") Long spuId, @Param("code") int code);

}
