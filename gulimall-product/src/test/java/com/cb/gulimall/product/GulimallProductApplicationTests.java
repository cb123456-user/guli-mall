package com.cb.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cb.gulimall.product.entity.BrandEntity;
import com.cb.gulimall.product.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {

//        BrandEntity entity = new BrandEntity();
//        entity.setName("test");
//        brandService.save(entity);

//        BrandEntity entity = new BrandEntity();
//        entity.setBrandId(6L);
//        entity.setDescript("test");
//        brandService.updateById(entity);


//        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 6));
//        list.forEach(System.out::println);

//        List<BrandEntity> dataList = brandService.list(new LambdaQueryWrapper<BrandEntity>()
//                .eq(BrandEntity::getBrandId, 6));
//        dataList.forEach(System.out::println);


    }

}
