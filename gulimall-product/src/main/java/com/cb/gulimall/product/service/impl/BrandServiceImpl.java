package com.cb.gulimall.product.service.impl;

import com.alibaba.nacos.client.utils.StringUtils;
import com.cb.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.BrandDao;
import com.cb.gulimall.product.entity.BrandEntity;
import com.cb.gulimall.product.service.BrandService;
import org.springframework.transaction.annotation.Transactional;


@Service("brandService")
public class BrandServiceImpl extends ServiceImpl<BrandDao, BrandEntity> implements BrandService {


    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<BrandEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().eq(BrandEntity::getBrandId, key)
                    .or().like(BrandEntity::getName, key);
        }
        IPage<BrandEntity> page = this.page(
                new Query<BrandEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void updateCascade(BrandEntity brand) {
        // 更改品牌
        this.updateById(brand);

        // 更改级联属性，保证冗余数据一致性
        String brandName = brand.getName();
        if (!StringUtils.isEmpty(brandName)) {
            // 品牌
            categoryBrandRelationService.updateBrand(brand.getBrandId(), brand.getName());
            // todo 其他

        }

    }

}