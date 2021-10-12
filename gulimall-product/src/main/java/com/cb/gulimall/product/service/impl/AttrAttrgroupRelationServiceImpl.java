package com.cb.gulimall.product.service.impl;

import com.cb.gulimall.product.vo.AttrRelationVo;
import com.cb.gulimall.product.vo.AttrRespVo;
import com.cb.gulimall.product.vo.AttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.cb.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.cb.gulimall.product.service.AttrAttrgroupRelationService;


@Service("attrAttrgroupRelationService")
public class AttrAttrgroupRelationServiceImpl extends ServiceImpl<AttrAttrgroupRelationDao, AttrAttrgroupRelationEntity> implements AttrAttrgroupRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrAttrgroupRelationEntity> page = this.page(
                new Query<AttrAttrgroupRelationEntity>().getPage(params),
                new QueryWrapper<AttrAttrgroupRelationEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public void removeAttrRelations(List<AttrRelationVo> attrRelationVos) {
        // vo转换为po
        List<AttrAttrgroupRelationEntity> entities = attrRelationVos.stream().map(it -> {
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(it, relation);
            return relation;
        }).collect(Collectors.toList());
        // 批量删除
        baseMapper.removeAttrRelations(entities);
    }

    @Override
    public void attrRelation(List<AttrVo> vos) {
        List<AttrAttrgroupRelationEntity> entities = vos.stream().map(it -> {
            AttrAttrgroupRelationEntity relation = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(it, relation);
            return relation;
        }).collect(Collectors.toList());
        this.saveBatch(entities);
    }

}