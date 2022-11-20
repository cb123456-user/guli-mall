package com.cb.gulimall.search.controller;

import com.cb.common.exception.BzipCodeEnum;
import com.cb.common.to.es.SkuEsModel;
import com.cb.common.utils.R;
import com.cb.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/search/save")
public class ProductSaveController {

    @Autowired
    private ProductSaveService productSaveService;

    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean status;
        try {
            status = productSaveService.productStatusUp(skuEsModelList);
        } catch (Exception e) {
            log.error("ProductSaveController商品上架异常: {}", e);
            return R.error(BzipCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BzipCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (status) {
            return R.ok();
        } else {
            return R.error(BzipCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BzipCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
