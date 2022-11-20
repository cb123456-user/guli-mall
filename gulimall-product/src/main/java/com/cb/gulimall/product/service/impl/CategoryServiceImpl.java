package com.cb.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cb.gulimall.product.service.CategoryBrandRelationService;
import com.cb.gulimall.product.vo.Catalog3Vo;
import com.cb.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
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

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redisson;

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


//    @Caching(evict={
//            @CacheEvict(value = "category",key="'getCategoryLevel1s'"),
//            @CacheEvict(value = "category",key="'getCatalogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)
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
     * 1、每一个需要缓存的数据都要指定缓存分区【按业务类型分】
     * 2、@Cacheable(value = "category")
     *    代表当前方法的结果需要缓存，如果缓存中有，直接走缓存
     *    如果缓存中没有，会调方法，最后将方法的结果放入缓存
     * 3、默认行为
     *    如果缓存中有，方法不调用
     *    key默认自动生成：category::SimpleKey []
     *    缓存的value值：默认用jdk序列化机制，将序列化后的数据存到redis
     *    默认ttl时间：-1
     * 4、自定义
     *    指定缓存key：key属性指定，接受SPEL，如果是字符串,需要加上''
     *    指定缓存数据的存活时间：配置文件修改ttl
     *    将数据保存为json格式：配置类设置序列化方式
     */
//    @Cacheable(value = "category",key="#root.methodName")
    @Cacheable(value = "category",key="'getCategoryLevel1s'",sync = true)
    @Override
    public List<CategoryEntity> getCategoryLevel1s() {
        System.out.println("getCategoryList ... ");
        return getCategoryList(0L);
    }

    private List<CategoryEntity> getCategoryList(Long parentCid) {
        List<CategoryEntity> entities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().lambda()
                .eq(CategoryEntity::getParentCid, parentCid));
        return entities;
    }

    @Cacheable(value = "category",key="'getCatalogJson'")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        System.out.println("查询数据库。。。");
        List<CategoryEntity> categoryEntities = list();
        // 1级分类
        List<CategoryEntity> categoryLevel1s = getParentCid(categoryEntities, 0L);
        if (categoryLevel1s == null) {
            return Collections.emptyMap();
        }
        Map<String, List<Catelog2Vo>> map = categoryLevel1s.stream().collect(
                // key-1级分类id, value-2级分类(3级分类)
                Collectors.toMap(
                        k -> k.getCatId().toString(),
                        l1 -> {
                            // 2级分类
                            List<CategoryEntity> Category2List = getParentCid(categoryEntities, l1.getCatId());
                            List<Catelog2Vo> catelog2Vos = null;
                            if (Category2List != null) {
                                // 2级分类转换
                                catelog2Vos = Category2List.stream().map(l2 -> {
                                    Catelog2Vo catelog2Vo = new Catelog2Vo()
                                            .setId(l2.getCatId().toString())
                                            .setName(l2.getName())
                                            .setCatalog1Id(l1.getCatId().toString());
                                    // 3级分类
                                    List<CategoryEntity> category3List = getParentCid(categoryEntities, l2.getCatId());
                                    if (category3List != null) {
                                        // 3级分类转换
                                        List<Catalog3Vo> catalog3Vos = category3List.stream().map(l3 -> {
                                            Catalog3Vo catalog3Vo = new Catalog3Vo()
                                                    .setId(l3.getCatId().toString())
                                                    .setName(l3.getName())
                                                    .setCatalog2Id(l2.getCatId().toString());
                                            return catalog3Vo;
                                        }).collect(Collectors.toList());
                                        catelog2Vo.setCatalog3List(catalog3Vos);
                                    }
                                    return catelog2Vo;
                                }).collect(Collectors.toList());
                            }
                            return catelog2Vos;
                        }
                )
        );
        return map;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonWithLock() {
        String catalog = redisTemplate.opsForValue().get("catalog");
        if (StringUtils.isEmpty(catalog)) {
//            System.out.println("缓存未命中.....");
            Map<String, List<Catelog2Vo>> catalogDb = getCatalogJsonWithRedissonLock();
//            String catalogStr = JSON.toJSONString(catalogDb);
//            redisTemplate.opsForValue().set("catalog", catalogStr);
            return catalogDb;
        }
        Map<String, List<Catelog2Vo>> catalogMap = JSON.parseObject(catalog, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return catalogMap;
    }

    /**
     * 基于redisson实现分布式锁
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithRedissonLock() {
        // 锁名相同就是同一把锁
        RLock lock = redisson.getLock("catalogJson-lock");
        // 阻塞拿锁
        lock.lock();
        Map<String, List<Catelog2Vo>> data;
        try {
            data = getDataFromDb();
        } finally {
            lock.unlock();
        }
        return data;
    }

    /**
     * 基于redis实现分布式锁
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithRedisLock() {
        // uudi做为value，避免误释放锁
        String uuid = UUID.randomUUID().toString();
        // 设置分布式锁 SET key value EX NX，保证设置分布式锁原子性，锁需要设置过期时间，避免死锁产生(执行业务前断电了/业务异常)
        // 锁自动续期，可以考虑将过期时间设置长一点
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            // 获取锁成功,执行业务
            Map<String, List<Catelog2Vo>> data;
            // try捕获，业务完成/业务异常释放锁
            try {
                data = getDataFromDb();
            } finally {
                // lua脚本释放锁，保证释放锁原子性
                // 释放锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                //删除锁
                redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }
            return data;
        } else {
            System.out.println("获取分布式锁失败...等待重试");
            // 重试，避免重试太频繁，睡眠一会
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
            }
            return getCatalogJsonWithRedisLock();
        }
    }

    /**
     * 本地锁：synchronized
     * 由于springboot是单实例，所以this为同一把锁
     * 查询缓存、查询数据库、设置缓存需要一起完成后才可以释放锁，保证原子性
     */
    public Map<String, List<Catelog2Vo>> getCatalogJsonWithLocalLock() {
        synchronized (this) {
            return getDataFromDb();
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        // 查询缓存，确认为空时才走数据库
        String catalog = redisTemplate.opsForValue().get("catalog");
        if (!StringUtils.isEmpty(catalog)) {
//            System.out.println("命中缓存。。。");
            Map<String, List<Catelog2Vo>> catalogMap = JSON.parseObject(catalog, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return catalogMap;
        }
        System.out.println("查询数据库。。。");
        List<CategoryEntity> categoryEntities = list();
        // 1级分类
        List<CategoryEntity> categoryLevel1s = getParentCid(categoryEntities, 0L);
        if (categoryLevel1s == null) {
            return Collections.emptyMap();
        }
        Map<String, List<Catelog2Vo>> map = categoryLevel1s.stream().collect(
                // key-1级分类id, value-2级分类(3级分类)
                Collectors.toMap(
                        k -> k.getCatId().toString(),
                        l1 -> {
                            // 2级分类
                            List<CategoryEntity> Category2List = getParentCid(categoryEntities, l1.getCatId());
                            List<Catelog2Vo> catelog2Vos = null;
                            if (Category2List != null) {
                                // 2级分类转换
                                catelog2Vos = Category2List.stream().map(l2 -> {
                                    Catelog2Vo catelog2Vo = new Catelog2Vo()
                                            .setId(l2.getCatId().toString())
                                            .setName(l2.getName())
                                            .setCatalog1Id(l1.getCatId().toString());
                                    // 3级分类
                                    List<CategoryEntity> category3List = getParentCid(categoryEntities, l2.getCatId());
                                    if (category3List != null) {
                                        // 3级分类转换
                                        List<Catalog3Vo> catalog3Vos = category3List.stream().map(l3 -> {
                                            Catalog3Vo catalog3Vo = new Catalog3Vo()
                                                    .setId(l3.getCatId().toString())
                                                    .setName(l3.getName())
                                                    .setCatalog2Id(l2.getCatId().toString());
                                            return catalog3Vo;
                                        }).collect(Collectors.toList());
                                        catelog2Vo.setCatalog3List(catalog3Vos);
                                    }
                                    return catelog2Vo;
                                }).collect(Collectors.toList());
                            }
                            return catelog2Vos;
                        }
                )
        );
        /**
         * 缓存查询与缓存设置分离，非原子性，会多次查询数据库
         * 查询数据库，返回值，再将值写入缓存，写入前锁释放了，其他线程在写入缓存前又查询了数据库
         */
        String catalogStr = JSON.toJSONString(map);
        redisTemplate.opsForValue().set("catalog", catalogStr);
        return map;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> categoryEntityList, Long parentCid) {
        return categoryEntityList.stream().filter(it -> it.getParentCid().equals(parentCid)).collect(Collectors.toList());
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