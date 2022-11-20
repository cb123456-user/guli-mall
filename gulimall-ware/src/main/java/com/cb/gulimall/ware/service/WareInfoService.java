package com.cb.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.ware.entity.WareInfoEntity;
import com.cb.gulimall.ware.vo.FareVo;

import java.util.Map;

/**
 * 仓库信息
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:28:06
 */
public interface WareInfoService extends IService<WareInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    FareVo fare(Long addrId);
}

