package com.cb.gulimall.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cb.common.constant.ProductConstant;
import com.cb.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.cb.gulimall.product.dao.AttrGroupDao;
import com.cb.gulimall.product.dao.CategoryDao;
import com.cb.gulimall.product.entity.*;
import com.cb.gulimall.product.service.CategoryService;
import com.cb.gulimall.product.vo.AttrRespVo;
import com.cb.gulimall.product.vo.AttrVo;
import com.cb.gulimall.product.vo.SpuGroupAttrVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.AttrDao;
import com.cb.gulimall.product.service.AttrService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private AttrGroupDao attrGroupDao;

    @Autowired
    private CategoryService categoryService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    @Transactional
    public void saveAttr(AttrVo attr) {

        // 1、保存基本信息
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.save(attrEntity);

        // 基础属性才关联分组
        if (checkBaseAttr(attrEntity.getAttrType()) && attrEntity.getAttrType() != null) {
            // 2、保存关联关系
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId()).setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(relationEntity);
        }
    }

    @Override
    public PageUtils attrPage(Map<String, Object> params, Long catelogId, String attrType) {

        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        int attrTypeValue = "base".equalsIgnoreCase(attrType) ?
                ProductConstant.attrTypeEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.attrTypeEnum.ATTR_TYPE_SALE.getCode();
        queryWrapper.lambda().eq(AttrEntity::getAttrType, attrTypeValue);


        // catelog_id
        if (catelogId != 0) {
            queryWrapper.lambda().eq(AttrEntity::getCatelogId, catelogId);
        }

        // attr_id or attr_name
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().and(it -> {
                it.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key);
            });
        }

        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                queryWrapper
        );

        // 额外设置其他属性
        List<AttrRespVo> respVoList = page.getRecords().stream().map(it -> {
                    AttrRespVo respVo = new AttrRespVo();
                    BeanUtils.copyProperties(it, respVo);

                    // 分类
                    CategoryEntity categoryEntity = categoryDao.selectById(it.getCatelogId());
                    if (categoryEntity != null) {
                        respVo.setCatelogName(categoryEntity.getName());
                    }

                    // 基础属性才关联分组
                    if (checkBaseAttr(it.getAttrType())) {
                        // 属性分组
                        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(
                                new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                                        .eq(AttrAttrgroupRelationEntity::getAttrId, it.getAttrId())
                        );
                        if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                            if (attrGroupEntity != null) {
                                respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                            }
                        }
                    }

                    return respVo;
                }
        ).collect(Collectors.toList());

        PageUtils pageUtils = new PageUtils(page);
        pageUtils.setList(respVoList);

        return pageUtils;
    }

    @Cacheable(value = "attr", key = "#root.method.name + #root.args")
    @Override
    public AttrRespVo getAttrRespVo(Long attrId) {
        AttrRespVo attrRespVo = new AttrRespVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, attrRespVo);

        // 分类完整路径
        Long[] categoryPath = categoryService.getCategoryPath(attrEntity.getCatelogId());
        attrRespVo.setCatelogPath(categoryPath);

        // 分类名
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrRespVo.setCatelogName(categoryEntity.getName());
        }

        // 基础属性才有属性分组
        if (checkBaseAttr(attrEntity.getAttrType())) {
            // 属性分组
            AttrAttrgroupRelationEntity attrgroupRelation = attrAttrgroupRelationDao.selectOne(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attrEntity.getAttrId())
            );
            if (attrgroupRelation != null) {
                AttrGroupEntity attrGroup = attrGroupDao.selectById(attrgroupRelation.getAttrGroupId());
                if (attrGroup != null) {
                    attrRespVo.setGroupName(attrGroup.getAttrGroupName()).setAttrGroupId(attrGroup.getAttrGroupId());
                }
            }
        }


        return attrRespVo;
    }

    @Override
    public void updateAttr(AttrVo attr) {
        // 1、保存基本信息
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        this.updateById(attrEntity);

        // 基础属性才关联分组
        if (checkBaseAttr(attrEntity.getAttrType())) {
            // 2、关联关系存在关系，否则新增
            AttrAttrgroupRelationEntity attrgroupRelation = new AttrAttrgroupRelationEntity();
            attrgroupRelation.setAttrId(attr.getAttrId()).setAttrGroupId(attr.getAttrGroupId());
            Integer count = attrAttrgroupRelationDao.selectCount(
                    new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                            .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationDao.update(attrgroupRelation, new UpdateWrapper<AttrAttrgroupRelationEntity>().lambda()
                        .eq(AttrAttrgroupRelationEntity::getAttrId, attr.getAttrId()));
            } else {
                attrAttrgroupRelationDao.insert(attrgroupRelation);
            }
        }
    }

    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        // 从关联表查询属性id
        List<AttrAttrgroupRelationEntity> relations = attrAttrgroupRelationDao.selectList(
                new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                        .eq(AttrAttrgroupRelationEntity::getAttrGroupId, attrgroupId));

        List<Long> attrIds = relations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(attrIds)) {
            return null;
        }
        List<AttrEntity> attrs = baseMapper.selectBatchIds(attrIds);
        return attrs;
    }

    @Override
    public PageUtils getNoRelationAttrPage(Map<String, Object> params, Long attrgroupId) {
        // 1、当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroup = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroup.getCatelogId();
        // 2、当前分组只能是所属的分类下分组没有引用的属性
        // 2.1 所属分类下全部分组
        List<AttrGroupEntity> attrGroups = attrGroupDao.selectList(new QueryWrapper<AttrGroupEntity>().lambda()
                .eq(AttrGroupEntity::getCatelogId, catelogId));
        List<Long> attrGroupIds = attrGroups.stream().map(AttrGroupEntity::getAttrGroupId).collect(Collectors.toList());
        // 2.2 关联表中所有已经关联的属性
        List<AttrAttrgroupRelationEntity> attrgroupRelations = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>().lambda()
                .in(AttrAttrgroupRelationEntity::getAttrGroupId, attrGroupIds));
        List<Long> attrIds = attrgroupRelations.stream().map(AttrAttrgroupRelationEntity::getAttrId).collect(Collectors.toList());
        // 2.3 排除已关联的基本属性
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(AttrEntity::getAttrType, ProductConstant.attrTypeEnum.ATTR_TYPE_BASE.getCode())
                .eq(AttrEntity::getCatelogId, catelogId);
        if (!CollectionUtils.isEmpty(attrIds)) {
            queryWrapper.lambda().notIn(AttrEntity::getAttrId, attrIds);
        }
        // 2.4 条件组装
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            queryWrapper.lambda().and(it -> it.eq(AttrEntity::getAttrId, key).or().like(AttrEntity::getAttrName, key));
        }
        IPage<AttrEntity> iPage = this.baseMapper.selectPage(new Query<AttrEntity>().getPage(params), queryWrapper);
        PageUtils pageUtils = new PageUtils(iPage);
        return pageUtils;
    }

    @Override
    public List<Long> getSearchAttrIds(List<Long> attrIds) {
        List<AttrEntity> attrEntities = list(new QueryWrapper<AttrEntity>().lambda()
                .select(AttrEntity::getAttrId)
                .eq(AttrEntity::getSearchType, 1)
                .in(AttrEntity::getAttrId, attrIds)
        );
        return attrEntities.stream().map(AttrEntity::getAttrId).collect(Collectors.toList());
    }

    @Override
    public List<SpuGroupAttrVo> getSpuGroupAttrs(Long spuId, Long catalogId) {
        return this.baseMapper.getSpuGroupAttrs(spuId, catalogId);
    }

    /**
     * 检查是否为基础属性
     *
     * @param attrType
     * @return
     */
    private Boolean checkBaseAttr(Integer attrType) {
        return ProductConstant.attrTypeEnum.ATTR_TYPE_BASE.getCode() == attrType;
    }
}