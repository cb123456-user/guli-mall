package com.cb.gulimall.product.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.ProductAttrValueDao;
import com.cb.gulimall.product.entity.ProductAttrValueEntity;
import com.cb.gulimall.product.service.ProductAttrValueService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("productAttrValueService")
public class ProductAttrValueServiceImpl extends ServiceImpl<ProductAttrValueDao, ProductAttrValueEntity> implements ProductAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<ProductAttrValueEntity> page = this.page(
                new Query<ProductAttrValueEntity>().getPage(params),
                new QueryWrapper<ProductAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveAttrValues(List<ProductAttrValueEntity> valueEntityList) {
        if(CollectionUtils.isEmpty(valueEntityList)){
            return;
        }
        this.saveBatch(valueEntityList);
    }

    @Override
    public List<ProductAttrValueEntity> attrListforspu(Long spuId) {

        return list(new QueryWrapper<ProductAttrValueEntity>().lambda()
                .eq(ProductAttrValueEntity::getSpuId, spuId));
    }

    @Override
    @Transactional
    public void updateBySpuId(Long spuId, List<ProductAttrValueEntity> entities) {
        // 1、删除旧数据
        remove(new QueryWrapper<ProductAttrValueEntity>().lambda()
                .eq(ProductAttrValueEntity::getSpuId, spuId));

        // 2、批量保存
        List<ProductAttrValueEntity> entityList = entities.stream().map(it -> {
            ProductAttrValueEntity entity = new ProductAttrValueEntity();
            BeanUtils.copyProperties(it, entity);
            entity.setSpuId(spuId);
            return entity;
        }).collect(Collectors.toList());
        saveBatch(entityList);
    }

}