package com.cb.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cb.common.utils.PageUtils;
import com.cb.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.cb.gulimall.product.vo.AttrRelationVo;
import com.cb.gulimall.product.vo.AttrVo;

import java.util.List;
import java.util.Map;

/**
 * 属性&属性分组关联
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
public interface AttrAttrgroupRelationService extends IService<AttrAttrgroupRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void removeAttrRelations(List<AttrRelationVo> attrRelationVos);

    void attrRelation(List<AttrVo> vos);
}

