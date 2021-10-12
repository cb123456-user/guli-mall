package com.cb.gulimall.product.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cb.common.valid.AddGroup;
import com.cb.common.valid.UpdateGroup;
import com.cb.common.valid.UpdateStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cb.gulimall.product.entity.BrandEntity;
import com.cb.gulimall.product.service.BrandService;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.R;

import javax.validation.Valid;


/**
 * 品牌
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
//    @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
//    @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     * BindingResult-可以拿到参数校验结果，但是一般用全局异常处理器统一处理异常
     * JSR303校验生效需要加注解@Valid / @Validated - 可以指定分组校验
     * 开启分组校验后，未标注分组的属性不会校验，只会在未开启分组校验时生效
     */
    @RequestMapping("/save")
//    @RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand /*, BindingResult bindingResult*/) {
//        if (bindingResult.hasErrors()) {
//            Map<String, String> map = new HashMap<>();
//            bindingResult.getFieldErrors().forEach(it -> map.put(it.getField(), it.getDefaultMessage()));
//            return R.error().put("data", map);
//        }

        brandService.save(brand);

        return R.ok();
    }

    /**
     * 修改
     *
     */
    @RequestMapping("/update")
//    @RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand) {
        brandService.updateCascade(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update/status")
//    @RequiresPermissions("product:brand:update")
    public R updatetatus(@Validated({UpdateStatus.class}) @RequestBody BrandEntity brand) {
        BrandEntity entity = new BrandEntity().setBrandId(brand.getBrandId()).setShowStatus(brand.getShowStatus());
        brandService.updateById(entity);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
//    @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}
