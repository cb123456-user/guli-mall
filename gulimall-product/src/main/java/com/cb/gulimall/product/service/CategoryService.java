package com.cb.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.product.entity.CategoryEntity;
import com.cb.gulimall.product.vo.Catelog2Vo;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listTree();

    void deleteByIds(List<Long> catIds);

    Long[] getCategoryPath(Long catelogId);

    void updateCascade(CategoryEntity category);

    List<CategoryEntity> getCategoryLevel1s();

    Map<String, List<Catelog2Vo>> getCatalogJson();
}

