package com.cb.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.coupon.entity.MemberPriceEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品会员价格
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:10:55
 */
public interface MemberPriceService extends IService<MemberPriceEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveMemberPrices(List<MemberPriceEntity> memberPriceEntities);
}

