package com.cb.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.cb.gulimall.product.entity.AttrEntity;
import com.cb.gulimall.product.service.AttrAttrgroupRelationService;
import com.cb.gulimall.product.service.AttrService;
import com.cb.gulimall.product.service.CategoryService;
import com.cb.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.cb.gulimall.product.vo.AttrRelationVo;
import com.cb.gulimall.product.vo.AttrVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.cb.gulimall.product.entity.AttrGroupEntity;
import com.cb.gulimall.product.service.AttrGroupService;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.R;


/**
 * 属性分组
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private AttrService attrService;

    @Autowired
    private AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 列表
     * catelogId = 0，默认查询全部属性分组
     */
    @RequestMapping("/list/{catelogId}")
//    @RequiresPermissions("product:attrgroup:list")
    public R list(
            @RequestParam Map<String, Object> params,
            @PathVariable("catelogId") Long catelogId
    ) {
//        PageUtils page = attrGroupService.queryPage(params);

        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     *  获取分类下所有分组&关联属性
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId){
        List<AttrGroupWithAttrsVo> voList = attrGroupService.getAttrGroupWithAttrByCatelogId(catelogId);
        return R.ok().put("data", voList);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{attrGroupId}")
//    @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        // 查询属性分组信息
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        // 查询分类完整路径
        Long catelogId = attrGroup.getCatelogId();
        Long[] catelogPath = categoryService.getCategoryPath(catelogId);
        attrGroup.setCatelogPath(catelogPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

    /**
     * 获取属性分组的关联的所有属性
     *
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R getAttrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> data = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", data);
    }

    /**
     * 删除属性与分组的关联关系
     *
     * @param attrRelationVos
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R removeAttrRelation(@RequestBody List<AttrRelationVo> attrRelationVos) {
        attrAttrgroupRelationService.removeAttrRelations(attrRelationVos);
        return R.ok();
    }

    /**
     * 获取属性分组没有关联的其他属性
     *
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R getAttrRelation(@RequestParam Map<String, Object> params,
                             @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.getNoRelationAttrPage(params, attrgroupId);
        return R.ok().put("data", page);
    }

    /**
     * 添加属性与分组关联关系
     * @param vos
     * @return
     */
    @PostMapping("/attr/relation")
    public R attrRelation(@RequestBody List<AttrVo> vos){
        attrAttrgroupRelationService.attrRelation(vos);
        return R.ok();
    }

}
