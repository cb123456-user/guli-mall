package com.cb.gulimall.product.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.SkuImagesDao;
import com.cb.gulimall.product.entity.SkuImagesEntity;
import com.cb.gulimall.product.service.SkuImagesService;
import org.springframework.util.CollectionUtils;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesDao, SkuImagesEntity> implements SkuImagesService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuImagesEntity> page = this.page(
                new Query<SkuImagesEntity>().getPage(params),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuImages(List<SkuImagesEntity> skuImagesEntities) {
        if(CollectionUtils.isEmpty(skuImagesEntities)){
            return;
        }
        this.saveBatch(skuImagesEntities);
    }

    @Override
    public List<SkuImagesEntity> getSkuImagesBySkuId(Long skuId) {
        return this.list(new QueryWrapper<SkuImagesEntity>().lambda().eq(SkuImagesEntity::getSkuId, skuId));
    }

}