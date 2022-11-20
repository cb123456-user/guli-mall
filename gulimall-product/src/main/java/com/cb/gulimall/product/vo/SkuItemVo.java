package com.cb.gulimall.product.vo;

import com.cb.gulimall.product.entity.SkuImagesEntity;
import com.cb.gulimall.product.entity.SkuInfoEntity;
import com.cb.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;

import java.util.List;

@Data
public class SkuItemVo {

    // 1.sku基本信息
    private SkuInfoEntity info;

    // 2.sku图片
    private List<SkuImagesEntity> images;

    // 3.spu的全部销售属性
    private List<SkuAttrVo> saleAttr;

    // 4.spu的商品介绍
    private SpuInfoDescEntity desc;

    // 5.spu的规格参数
    private List<SpuGroupAttrVo> groupAttrs;

    // 库存
    Boolean hasStock = true;

}
