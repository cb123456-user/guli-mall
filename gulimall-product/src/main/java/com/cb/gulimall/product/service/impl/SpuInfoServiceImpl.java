package com.cb.gulimall.product.service.impl;

import com.cb.common.to.SkuReductionTo;
import com.cb.common.to.SpuBoundsTo;
import com.cb.common.utils.R;
import com.cb.gulimall.product.entity.*;
import com.cb.gulimall.product.feign.CouponFeignService;
import com.cb.gulimall.product.service.*;
import com.cb.gulimall.product.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {


    @Autowired
    private SpuInfoDescService spuInfoDescService;

    @Autowired
    private SpuImagesService spuImagesService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private ProductAttrValueService productAttrValueService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SkuImagesService skuImagesService;

    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * todo ???????????????????????????
     *
     * @param vo
     */
    @Override
    @Transactional
    public void saveApuInfo(SpuSaveVo vo) {

        // 1.??????spu???????????? pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date()).setUpdateTime(new Date());
        this.saveSpuInfo(spuInfoEntity);

        // 2.??????spu?????????????????? pms_spu_info_desc
        List<String> voDecripts = vo.getDecript();
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId()).setDecript(String.join(",", voDecripts));
        spuInfoDescService.saveDecript(spuInfoDescEntity);

        // 3.??????spu???????????? pms_spu_images
        List<String> images = vo.getImages();
        List<SpuImagesEntity> spuImagesEntities = images.stream().map(it -> {
            SpuImagesEntity imagesEntity = new SpuImagesEntity();
            imagesEntity.setSpuId(spuInfoEntity.getId()).setImgUrl(it);
            return imagesEntity;
        }).collect(Collectors.toList());
        spuImagesService.saveSpuImages(spuImagesEntities);

        // 4.??????spu?????????????????? pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        List<ProductAttrValueEntity> valueEntityList = baseAttrs.stream().map(it -> {
            ProductAttrValueEntity valueEntity = new ProductAttrValueEntity();
            valueEntity.setSpuId(spuInfoEntity.getId()).setAttrId(it.getAttrId())
                    .setAttrValue(it.getAttrValues()).setQuickShow(it.getShowDesc());
            AttrEntity attrEntity = attrService.getById(it.getAttrId());
            valueEntity.setAttrName(attrEntity.getAttrName());
            return valueEntity;
        }).collect(Collectors.toList());
        productAttrValueService.saveAttrValues(valueEntityList);

        // 5.??????spu??????????????? gulimall-sms->sms_spu_bounds
        Bounds bounds = vo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.save(spuBoundsTo);
        if (!r.getCode().equals(0)) {
            log.error("spu {} ?????????????????????????????????", spuInfoEntity.getId());
        }

        // 6.??????sku??????
        // 6.1??????sku???????????? pms_sku_info
        List<Skus> skus = vo.getSkus();
        if (!CollectionUtils.isEmpty(skus)) {
            for (Skus item : skus) {
                String defaultImage = "";
                if (!CollectionUtils.isEmpty(item.getImages())) {
                    for (Images it : item.getImages()) {
                        if (it.getDefaultImg() == 1) {
                            defaultImage = it.getImgUrl();
                            break;
                        }
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(item, skuInfoEntity);
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId()).setBrandId(spuInfoEntity.getBrandId())
                        .setSpuId(spuInfoEntity.getId()).setSaleCount(0L).setSkuDefaultImg(defaultImage);
                skuInfoService.save(skuInfoEntity);

                // 6.2??????sku???????????? pms_sku_images?????????????????????image????????????????????????
                List<SkuImagesEntity> skuImagesEntities = item.getImages().stream()
                        .filter(it -> !StringUtils.isEmpty(it.getImgUrl()))
                        .map(it -> {
                            SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                            skuImagesEntity.setSkuId(skuInfoEntity.getSkuId()).setDefaultImg(it.getDefaultImg())
                                    .setImgUrl(it.getImgUrl());
                            return skuImagesEntity;
                        }).collect(Collectors.toList());
                skuImagesService.saveSkuImages(skuImagesEntities);

                // 6.3??????sku?????????????????? pms_sku_sale_attr_value
                List<Attr> attr = item.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attr.stream().map(it -> {
                    SkuSaleAttrValueEntity saleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(it, saleAttrValueEntity);
                    saleAttrValueEntity.setSkuId(skuInfoEntity.getSkuId());
                    return saleAttrValueEntity;
                }).collect(Collectors.toList());
                skuSaleAttrValueService.saveSkuSaleAttrValues(skuSaleAttrValueEntities);

                // 6.4??????sku????????????????????????????????????????????? gulimall-sms->sms_sku_ladder/sms_sku_full_reduction/sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(item, skuReductionTo);
                skuReductionTo.setSkuId(skuInfoEntity.getSkuId());
                R r1 = couponFeignService.saveInfo(skuReductionTo);
                if (!r1.getCode().equals(0)) {
                    log.error("sku {} ?????????????????????????????????", skuInfoEntity.getSkuId());
                }
            }
        }
    }

    @Override
    public void saveSpuInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryByCondition(Map<String, Object> params) {

        /**
         *    key: '??????',//???????????????
         *    catelogId: 6,//????????????id
         *    brandId: 1,//??????id
         *    status: 0,//????????????
         */
        QueryWrapper<SpuInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().and(it ->
                    it.eq(SpuInfoEntity::getId, key).or().like(SpuInfoEntity::getSpuName, key)
            );
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            queryWrapper.lambda().eq(SpuInfoEntity::getCatalogId, catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            queryWrapper.lambda().eq(SpuInfoEntity::getBrandId, brandId);
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            queryWrapper.lambda().eq(SpuInfoEntity::getPublishStatus, status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

}