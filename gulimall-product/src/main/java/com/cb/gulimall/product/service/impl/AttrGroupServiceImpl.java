package com.cb.gulimall.product.service.impl;

import com.cb.gulimall.product.entity.AttrEntity;
import com.cb.gulimall.product.service.AttrService;
import com.cb.gulimall.product.vo.AttrGroupWithAttrsVo;
import org.springframework.beans.BeanUtils;
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

import com.cb.gulimall.product.dao.AttrGroupDao;
import com.cb.gulimall.product.entity.AttrGroupEntity;
import com.cb.gulimall.product.service.AttrGroupService;
import org.springframework.util.StringUtils;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * catelogId = 0， 默认查询所有属性分组
     * key - 可查询attr_group_id或attr_group_name
     *
     * @param params
     * @param catelogId
     * @return
     */
    @Override
    public PageUtils queryPage(Map<String, Object> params, Long catelogId) {

        IPage<AttrGroupEntity> iPage = new Query<AttrGroupEntity>().getPage(params);
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.lambda().and(
                    it -> it.eq(AttrGroupEntity::getAttrGroupId, key)
                            .or().like(AttrGroupEntity::getAttrGroupName, key)
            );
        }

        // 全部查询
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(iPage, wrapper);
            return new PageUtils(page);
        }

        // 条件查询
        wrapper.lambda().eq(AttrGroupEntity::getCatelogId, catelogId);

        IPage<AttrGroupEntity> page = this.page(iPage, wrapper);

        return new PageUtils(page);
    }

    @Override
    public List<AttrGroupWithAttrsVo> getAttrGroupWithAttrByCatelogId(Long catelogId) {
        // 1.获取分类下的属性分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().lambda()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
        List<AttrGroupWithAttrsVo> attrsVos = attrGroupEntities.stream().map(it -> {
            AttrGroupWithAttrsVo vo = new AttrGroupWithAttrsVo();
            BeanUtils.copyProperties(it, vo);
            // 2.根据属性分组id获取属性
            List<AttrEntity> attrEntities = attrService.getRelationAttr(it.getAttrGroupId());
            vo.setAttrs(attrEntities);
            return vo;
        }).collect(Collectors.toList());
        return attrsVos;
    }

}