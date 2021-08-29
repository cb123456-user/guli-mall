package com.cb.gulimall.coupon.dao;

import com.cb.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:10:55
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
