package com.cb.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.cb.common.utils.R;
import com.cb.gulimall.ware.feign.MemberFeignSerive;
import com.cb.gulimall.ware.vo.FareVo;
import com.cb.gulimall.ware.vo.MemberAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Random;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.ware.dao.WareInfoDao;
import com.cb.gulimall.ware.entity.WareInfoEntity;
import com.cb.gulimall.ware.service.WareInfoService;
import org.springframework.util.StringUtils;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {

    @Autowired
    public MemberFeignSerive memberFeignSerive;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> queryWrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().eq(WareInfoEntity::getId, key)
                    .or().like(WareInfoEntity::getName, key)
                    .or().like(WareInfoEntity::getAddress, key)
                    .or().eq(WareInfoEntity::getAreacode, key);
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                queryWrapper
        );

        return new PageUtils(page);
    }

    @Override
    public FareVo fare(Long addrId) {
        FareVo fareVo = new FareVo();
        R response = memberFeignSerive.AddressInfo(addrId);
        MemberAddressVo memberAddressVo = response.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>() {
        });
        fareVo.setMemberAddressVo(memberAddressVo);
        if (memberAddressVo != null) {
            if (memberAddressVo != null) {
                String phone = memberAddressVo.getPhone();
                if (phone == null || phone.length() < 2) {
                    phone = new Random().nextInt(100) + "";
                }
                BigDecimal decimal = new BigDecimal(phone.substring(phone.length() - 1));
                fareVo.setFare(decimal);
            } else {
                fareVo.setFare(new BigDecimal("20"));
            }
        }

        return fareVo;
    }

}