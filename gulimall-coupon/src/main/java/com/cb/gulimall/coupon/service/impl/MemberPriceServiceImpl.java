package com.cb.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.coupon.dao.MemberPriceDao;
import com.cb.gulimall.coupon.entity.MemberPriceEntity;
import com.cb.gulimall.coupon.service.MemberPriceService;
import org.springframework.util.CollectionUtils;


@Service("memberPriceService")
public class MemberPriceServiceImpl extends ServiceImpl<MemberPriceDao, MemberPriceEntity> implements MemberPriceService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberPriceEntity> page = this.page(
                new Query<MemberPriceEntity>().getPage(params),
                new QueryWrapper<MemberPriceEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveMemberPrices(List<MemberPriceEntity> memberPriceEntities) {
        if(CollectionUtils.isEmpty(memberPriceEntities)){
            return;
        }
        this.saveBatch(memberPriceEntities);
    }

}