package com.cb.gulimall.product.service.impl;

import com.cb.gulimall.product.entity.SkuImagesEntity;
import com.cb.gulimall.product.entity.SpuInfoDescEntity;
import com.cb.gulimall.product.service.*;
import com.cb.gulimall.product.vo.SkuAttrVo;
import com.cb.gulimall.product.vo.SkuItemVo;
import com.cb.gulimall.product.vo.SpuGroupAttrVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.SkuInfoDao;
import com.cb.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private ThreadPoolExecutor threadPool;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<SkuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        /**
         * key: '华为',//检索关键字
         * catelogId: 0,
         * brandId: 0,
         * min: 0,
         * max: 0
         */
        QueryWrapper<SkuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().and(it -> it.eq(SkuInfoEntity::getSkuId, key).or().like(SkuInfoEntity::getSkuName, key));
        }

        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.lambda().eq(SkuInfoEntity::getCatalogId, catelogId);
        }

        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.lambda().eq(SkuInfoEntity::getBrandId, brandId);
        }

        String min = (String) params.get("min");
        if (!StringUtils.isEmpty(min)) {
            try {
                BigDecimal minPrice = new BigDecimal(min);
                if (new BigDecimal(0).compareTo(minPrice) == -1) {
                    queryWrapper.lambda().ge(SkuInfoEntity::getPrice, minPrice);
                }
            } catch (Exception e) {

            }
        }

        String max = (String) params.get("max");
        if (!StringUtils.isEmpty(max)) {
            try {
                BigDecimal maxPrice = new BigDecimal(max);
                if (new BigDecimal(0).compareTo(maxPrice) == -1) {
                    queryWrapper.lambda().le(SkuInfoEntity::getPrice, maxPrice);
                }
            } catch (Exception e) {

            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuInfoEntity> getSkusBySpuId(Long spuId) {
        List<SkuInfoEntity> list = list(new QueryWrapper<SkuInfoEntity>().lambda().eq(SkuInfoEntity::getSpuId, spuId));
        return list;
    }

    @Override
    public SkuItemVo skuItemBySkuId(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();
//        // 1.sku基本信息
//        SkuInfoEntity skuInfoEntity = getById(skuId);
//        skuItemVo.setInfo(skuInfoEntity);
//        Long spuId = skuInfoEntity.getSpuId();
//        Long catalogId = skuInfoEntity.getCatalogId();
//
//        // 2.sku图片
//        List<SkuImagesEntity> images = skuImagesService.getSkuImagesBySkuId(skuId);
//        skuItemVo.setImages(images);
//
//        // 3.spu的全部销售属性
//        List<SkuAttrVo> skuAttrVos = skuSaleAttrValueService.selectSkuAttrBySpuId(spuId);
//        skuItemVo.setSaleAttr(skuAttrVos);
//
//        // 4.spu的商品介绍
//        SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(spuId);
//        skuItemVo.setDesc(spuInfoDescEntity);
//
//        // 5.spu的规格参数
//        List<SpuGroupAttrVo> groups = attrService.getSpuGroupAttrs(spuId, catalogId);
//        skuItemVo.setGroupAttrs(groups);

        // 异步编排
        
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            // 1.sku基本信息
            SkuInfoEntity skuInfoEntity = getById(skuId);
            skuItemVo.setInfo(skuInfoEntity);
            return skuInfoEntity;
        }, threadPool);


        CompletableFuture<Void> imgFuture = CompletableFuture.runAsync(() -> {
            // 2.sku图片
            List<SkuImagesEntity> images = skuImagesService.getSkuImagesBySkuId(skuId);
            skuItemVo.setImages(images);
        }, threadPool);

        CompletableFuture<Void> attrFuture = infoFuture.thenAcceptAsync((res) -> {
            // 3.spu的全部销售属性
            List<SkuAttrVo> skuAttrVos = skuSaleAttrValueService.selectSkuAttrBySpuId(res.getSpuId());
            skuItemVo.setSaleAttr(skuAttrVos);
        }, threadPool);

        CompletableFuture<Void> desFuture = infoFuture.thenAcceptAsync((res) -> {
            // 4.spu的商品介绍
            SpuInfoDescEntity spuInfoDescEntity = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDescEntity);
        }, threadPool);

        CompletableFuture<Void> groupFuture = infoFuture.thenAcceptAsync((res) -> {
            // 5.spu的规格参数
            List<SpuGroupAttrVo> groups = attrService.getSpuGroupAttrs(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupAttrs(groups);
        }, threadPool);


        CompletableFuture.allOf(infoFuture, imgFuture, attrFuture, desFuture, groupFuture).get();

        return skuItemVo;
    }


}