package com.cb.gulimall.product.service.impl;

import com.cb.gulimall.product.vo.SkuAttrVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.SkuSaleAttrValueDao;
import com.cb.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.cb.gulimall.product.service.SkuSaleAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuSaleAttrValues(List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities) {
        if (CollectionUtils.isEmpty(skuSaleAttrValueEntities)) {
            return;
        }
        this.saveBatch(skuSaleAttrValueEntities);
    }

    @Override
    public List<SkuAttrVo> selectSkuAttrBySpuId(Long spuId) {
        return this.baseMapper.selectSkuAttrBySpuId(spuId);

    }

    @Override
    public List<String> getSkuSaleAttrValuesBySkuId(Long skuId) {
        return this.baseMapper.getSkuSaleAttrValuesBySkuId(skuId);
    }

}