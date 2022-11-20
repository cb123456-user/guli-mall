package com.cb.gulimall.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.cb.gulimall.product.entity.BrandEntity;
import com.cb.gulimall.product.vo.BrandVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cb.gulimall.product.entity.CategoryBrandRelationEntity;
import com.cb.gulimall.product.service.CategoryBrandRelationService;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.R;


/**
 * 品牌分类关联
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:categorybrandrelation:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 获取品牌关联的分类
     */
    @RequestMapping("/catelog/list")
//    @RequiresPermissions("product:categorybrandrelation:list")
    public R catelogList(@RequestParam Long brandId) {
        List<CategoryBrandRelationEntity> list = categoryBrandRelationService.catelogList(brandId);

        return R.ok().put("data", list);
    }

    /**
     * 获取分类关联的品牌
     */
    @GetMapping("/brands/list")
    public R getBrandsList(@RequestParam("catId") Long catId) {
        // 1.获取品牌
        List<BrandEntity> brands = categoryBrandRelationService.getBrandsByCatId(catId);
        // 2.数据转换
        List<BrandVo> voList = brands.stream().map(it -> {
            BrandVo vo = new BrandVo();
            vo.setBrandId(it.getBrandId()).setBrandName(it.getName());
            return vo;

        }).collect(Collectors.toList());
        // 3.返回
        return R.ok().put("data", voList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
//    @RequiresPermissions("product:categorybrandrelation:info")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     * 避免级联查询，保存时将冗余字段分类名、品牌名一起保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:categorybrandrelation:save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveDeatail(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:categorybrandrelation:update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:categorybrandrelation:delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
