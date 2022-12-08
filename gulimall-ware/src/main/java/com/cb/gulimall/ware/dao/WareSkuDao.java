package com.cb.gulimall.ware.dao;

import com.cb.gulimall.ware.entity.WareSkuEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cb.gulimall.ware.vo.StockVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品库存
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:28:06
 */
@Mapper
public interface WareSkuDao extends BaseMapper<WareSkuEntity> {

    void updateStock(@Param("stockVo") StockVo stockVo);

    Long getSkuStockskuId(@Param("skuId") Long skuId);

    List<Long> selectSkuHasStockWare(@Param("skuId") Long skuId);

   Long skuStockLock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

    void unLockStock(@Param("skuId") Long skuId, @Param("wareId") Long wareId, @Param("num") Integer num);

}
