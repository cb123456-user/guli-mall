package com.cb.gulimall.product.app;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cb.gulimall.product.entity.CategoryEntity;
import com.cb.gulimall.product.service.CategoryService;
import com.cb.common.utils.R;


/**
 * 商品三级分类
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询出所有的分类信息，并以tree结构返回
     */
    @RequestMapping("/list/tree")
//    @RequiresPermissions("product:category:list")
    public R list() {
        List<CategoryEntity> categoryEntityList = categoryService.listTree();

        return R.ok().put("data", categoryEntityList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
//    @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改排序
     */
    @RequestMapping("/update/sort")
//    @RequiresPermissions("product:category:update")
    public R updateSort(@RequestBody List<CategoryEntity> category) {
        categoryService.updateBatchById(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category) {
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody List<Long> catIds) {
        // 需要校验分类是否被引用，未引用才可以删除
//        categoryService.removeByIds(Arrays.asList(catIds));

        categoryService.deleteByIds(catIds);

        return R.ok();
    }

}
