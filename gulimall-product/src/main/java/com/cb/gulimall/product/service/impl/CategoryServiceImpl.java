package com.cb.gulimall.product.service.impl;

import com.cb.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.product.dao.CategoryDao;
import com.cb.gulimall.product.entity.CategoryEntity;
import com.cb.gulimall.product.service.CategoryService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    // 因为父类已经继承了BaseMapper，可以直接用baseMapper
//    @Autowired
//    private CategoryDao categoryDao;

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

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

    @Override
    public Long[] getCategoryPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        getParentPath(catelogId, path);
        // 完整路径需要从一级到三级，所以反转一下
        Collections.reverse(path);
        return path.toArray(new Long[path.size()]);
    }

    @Override
    @Transactional
    public void updateCascade(CategoryEntity category) {
        // 更新分类
        this.updateById(category);

        // 级联更新，保证冗余数据一致性
        String name = category.getName();
        if (!StringUtils.isEmpty(name)) {
            // 分类
            categoryBrandRelationService.updateCtegory(category.getCatId(), name);
        }
    }

    /**
     * 分类完整路径：三级到一级
     * 255,34,2
     *
     * @param catelogId
     * @param path
     * @return
     */
    private List<Long> getParentPath(Long catelogId, List<Long> path) {
        path.add(catelogId);
        CategoryEntity category = baseMapper.selectById(catelogId);
        if (0 != category.getParentCid()) {
            getParentPath(category.getParentCid(), path);
        }
        return path;
    }

    /**
     * 递归设置子分类
     *
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