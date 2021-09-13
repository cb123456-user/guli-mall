package com.cb.gulimall.product.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.CategoryDao;
import com.cb.gulimall.product.entity.CategoryEntity;
import com.cb.gulimall.product.service.CategoryService;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    // 因为父类已经继承了BaseMapper，可以直接用baseMapper
//    @Autowired
//    private CategoryDao categoryDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listTree() {
        // 1.查询出所有的分类
        List<CategoryEntity> entities = baseMapper.selectList(null);
        // 2.递归设置子分类
        // 2.1 找到1级分类 parent_cid = 0
        List<CategoryEntity> level1List = entities.stream()
                .filter(it -> it.getParentCid() == 0)
                .map(it -> {
                    // 递归拿子分类
                    it.setChildren(getChildren(it, entities));
                    // 有的sort为null，避免空指针
                    it.setSort(it.getSort() == null ? 0 : it.getSort());
                    return it;
                })
                .sorted(Comparator.comparing(CategoryEntity::getSort))
                .collect(Collectors.toList());

        return level1List;
    }

    @Override
    public void deleteByIds(List<Long> catIds) {
        // todo  需要校验分类是否被引用，未引用才可以删除

        baseMapper.deleteBatchIds(catIds);
    }

    /**
     * 递归设置子分类
     * @param root
     * @param entities
     * @return
     */
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> entities) {

        return entities.stream()
                .filter(it -> it.getParentCid().equals(root.getCatId()))
                .map(it -> {
                    it.setChildren(getChildren(it, entities));
                    // 有的sort为null，避免空指针
                    it.setSort(it.getSort() == null ? 0 : it.getSort());
                    return it;
                })
                .sorted(Comparator.comparing(CategoryEntity::getSort))
                .collect(Collectors.toList());
    }

}