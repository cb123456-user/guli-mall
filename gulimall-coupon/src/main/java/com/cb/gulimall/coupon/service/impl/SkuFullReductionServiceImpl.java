package com.cb.gulimall.coupon.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.cb.common.to.MemberPrice;
import com.cb.common.to.SkuReductionTo;
import com.cb.gulimall.coupon.entity.MemberPriceEntity;
import com.cb.gulimall.coupon.entity.SkuLadderEntity;
import com.cb.gulimall.coupon.service.MemberPriceService;
import com.cb.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.coupon.dao.SkuFullReductionDao;
import com.cb.gulimall.coupon.entity.SkuFullReductionEntity;
import com.cb.gulimall.coupon.service.SkuFullReductionService;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {


    @Autowired
    private SkuLadderService skuLadderService;

    @Autowired
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 1、折扣,数量>0
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        BeanUtil.copyProperties(skuReductionTo, skuLadderEntity);
        skuLadderEntity.setAddOther(1);
        if (skuReductionTo.getFullCount() > 0) {
            skuLadderService.save(skuLadderEntity);
        }

        // 2、满减,满减价格>0
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        BeanUtils.copyProperties(skuReductionTo, skuFullReductionEntity);
        skuFullReductionEntity.setAddOther(1);
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal(0)) == 1) {
            this.save(skuFullReductionEntity);
        }

        // 3、会员价格,会员价格>0
        List<MemberPrice> memberPrices = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntities = memberPrices.stream()
                .filter(it -> it.getPrice().compareTo(new BigDecimal(0)) == 1)
                .map(it -> {
                    MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                    memberPriceEntity.setAddOther(1).setMemberLevelId(it.getId())
                            .setMemberLevelName(it.getName()).setMemberPrice(it.getPrice())
                            .setSkuId(skuReductionTo.getSkuId());
                    return memberPriceEntity;
                }).collect(Collectors.toList());
        memberPriceService.saveMemberPrices(memberPriceEntities);
    }

}