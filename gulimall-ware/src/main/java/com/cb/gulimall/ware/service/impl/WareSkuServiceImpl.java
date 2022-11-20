package com.cb.gulimall.ware.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cb.common.utils.R;
import com.cb.gulimall.ware.entity.PurchaseDetailEntity;
import com.cb.gulimall.ware.feign.ProductFeignService;
import com.cb.gulimall.ware.vo.SkuHasStockVo;
import com.cb.gulimall.ware.vo.StockVo;
import lombok.extern.slf4j.Slf4j;
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

import com.cb.gulimall.ware.dao.WareSkuDao;
import com.cb.gulimall.ware.entity.WareSkuEntity;
import com.cb.gulimall.ware.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("wareSkuService")
@Slf4j
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private ProductFeignService productFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        /**
         * wareId: 123,//仓库id
         *    skuId: 123//商品id
         */
        QueryWrapper<WareSkuEntity> queryWrapper = new QueryWrapper<>();
        String skuId = (String) params.get("skuId");
        if (!StringUtils.isEmpty(skuId)) {
            queryWrapper.lambda().eq(WareSkuEntity::getSkuId, skuId);
        }
        String wareId = (String) params.get("wareId");
        if (!StringUtils.isEmpty(wareId)) {
            queryWrapper.lambda().like(WareSkuEntity::getWareId, wareId);
        }
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void addStock(StockVo stockVo) {
        // skuId无库存，新增; 有则更新,库存累加
        List<WareSkuEntity> entityList = list(new QueryWrapper<WareSkuEntity>().lambda()
                .eq(WareSkuEntity::getSkuId, stockVo.getSkuId())
                .eq(WareSkuEntity::getWareId, stockVo.getWareId())
        );
        if (!CollectionUtils.isEmpty(entityList)) {
            this.baseMapper.updateStock(stockVo);
            return;
        }

        WareSkuEntity wareSkuEntity = new WareSkuEntity();
        try {
            // 新增库存时冗余存储sku_name
            R info = productFeignService.info(stockVo.getSkuId());
            if (info.getCode() == 0) {
                String skuName = (String) BeanUtil.beanToMap(info.get("skuInfo")).get("skuName");
                wareSkuEntity.setSkuName(skuName);
            }
        } catch (Exception e) {
            log.warn("ware feign product fail, path： /product/skuinfo/info/{skuId}");
        }
        wareSkuEntity.setSkuId(stockVo.getSkuId()).setStock(stockVo.getStock()).setWareId(stockVo.getWareId())
                .setStockLocked(0);
        save(wareSkuEntity);
    }

    @Override
    public List<SkuHasStockVo> hasStock(List<Long> skuIdList) {
        List<SkuHasStockVo> stockVos = skuIdList.stream().map(skuId -> {
            SkuHasStockVo vo = new SkuHasStockVo();
            // 注意，库存可能为空，需要用包装类接收数据
            Long stock = baseMapper.getSkuStockskuId(skuId);
            vo.setSkuId(skuId).setHasStock(stock == null ? false : stock > 0);
            return vo;
        }).collect(Collectors.toList());
        return stockVos;
    }


}