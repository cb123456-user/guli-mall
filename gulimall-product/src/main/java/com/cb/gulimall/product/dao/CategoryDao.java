package com.cb.gulimall.product.dao;

import com.cb.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
