package com.cb.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cb.gulimall.product.dao.BrandDao;
import com.cb.gulimall.product.dao.CategoryDao;
import com.cb.gulimall.product.entity.BrandEntity;
import com.cb.gulimall.product.entity.CategoryEntity;
import com.cb.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.CategoryBrandRelationDao;
import com.cb.gulimall.product.entity.CategoryBrandRelationEntity;
import com.cb.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.util.StringUtils;


@Service("categoryBrandRelationService")
public class CategoryBrandRelationServiceImpl extends ServiceImpl<CategoryBrandRelationDao, CategoryBrandRelationEntity> implements CategoryBrandRelationService {


    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private BrandDao brandDao;

    @Autowired
    private BrandService brandService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryBrandRelationEntity> page = this.page(
                new Query<CategoryBrandRelationEntity>().getPage(params),
                new QueryWrapper<CategoryBrandRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryBrandRelationEntity> catelogList(Long brandId) {
        return baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().lambda()
                .eq(CategoryBrandRelationEntity::getBrandId, brandId));
    }

    @Override
    public void saveDeatail(CategoryBrandRelationEntity categoryBrandRelation) {
        Long brandId = categoryBrandRelation.getBrandId();
        Long catelogId = categoryBrandRelation.getCatelogId();
        BrandEntity brandEntity = brandDao.selectById(brandId);
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        categoryBrandRelation.setBrandName(brandEntity.getName()).setCatelogName(categoryEntity.getName());
        this.save(categoryBrandRelation);
    }

    @Override
    public void updateBrand(Long brandId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setBrandId(brandId).setBrandName(name);
        baseMapper.update(relationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().lambda().eq(CategoryBrandRelationEntity::getBrandId, brandId)
        );
    }

    @Override
    public void updateCtegory(Long catId, String name) {
        CategoryBrandRelationEntity relationEntity = new CategoryBrandRelationEntity();
        relationEntity.setCatelogId(catId).setCatelogName(name);
        baseMapper.update(relationEntity,
                new UpdateWrapper<CategoryBrandRelationEntity>().lambda().eq(CategoryBrandRelationEntity::getCatelogId, catId)
        );
    }

    @Override
    public List<BrandEntity> getBrandsByCatId(Long catId) {
        List<CategoryBrandRelationEntity> relationEntities = baseMapper.selectList(new QueryWrapper<CategoryBrandRelationEntity>().lambda()
                .eq(CategoryBrandRelationEntity::getCatelogId, catId));
        List<BrandEntity> brandEntityList = relationEntities.stream().map(it -> {
            BrandEntity brandEntity = brandService.getById(it.getBrandId());
            return brandEntity;
        }).collect(Collectors.toList());
        return brandEntityList;
    }

}