package com.cb.gulimall.ware.service.impl;

import com.cb.common.constant.WareConstant;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.ware.dao.WareOrderTaskDetailDao;
import com.cb.gulimall.ware.entity.WareOrderTaskDetailEntity;
import com.cb.gulimall.ware.service.WareOrderTaskDetailService;


@Service("wareOrderTaskDetailService")
public class WareOrderTaskDetailServiceImpl extends ServiceImpl<WareOrderTaskDetailDao, WareOrderTaskDetailEntity> implements WareOrderTaskDetailService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareOrderTaskDetailEntity> page = this.page(
                new Query<WareOrderTaskDetailEntity>().getPage(params),
                new QueryWrapper<WareOrderTaskDetailEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<WareOrderTaskDetailEntity> getOrderTaskDetailByTaskId(Long taskId) {
        return this.list(new QueryWrapper<WareOrderTaskDetailEntity>().lambda()
                .eq(WareOrderTaskDetailEntity::getTaskId, taskId)
                .eq(WareOrderTaskDetailEntity::getLockStatus, WareConstant.WareOrderTaskStatusEnum.LOCKED.getStatus())
        );
    }

}