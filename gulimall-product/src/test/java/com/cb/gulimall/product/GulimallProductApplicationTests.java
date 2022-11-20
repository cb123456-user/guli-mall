package com.cb.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cb.gulimall.product.dao.AttrDao;
import com.cb.gulimall.product.dao.SkuSaleAttrValueDao;
import com.cb.gulimall.product.entity.BrandEntity;
import com.cb.gulimall.product.service.BrandService;
import com.cb.gulimall.product.vo.SkuAttrVo;
import com.cb.gulimall.product.vo.SpuGroupAttrVo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private AttrDao attrDao;

    @Autowired
    private SkuSaleAttrValueDao skuSaleAttrValueDao;

    @Test
    public void testSkuAttrs(){
        List<SkuAttrVo> skuAttrVos = skuSaleAttrValueDao.selectSkuAttrBySpuId(13L);
        skuAttrVos.forEach(System.out::println);
    }

    @Test
    public void testRedission() {
        System.out.println(redissonClient);
    }

    @Test
    public void attr(){
        List<SpuGroupAttrVo> spuGroupAttrs = attrDao.getSpuGroupAttrs(13L, 225L);
        spuGroupAttrs.forEach(System.out::println);
    }

    @Test
    public void testRedis() {
        redisTemplate.opsForValue().set("hello", "word" + UUID.randomUUID());
    }

    @Test
    public void contextLoads() {

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
