package com.cb.gulimall.product.web;

import com.cb.gulimall.product.entity.CategoryEntity;
import com.cb.gulimall.product.service.CategoryService;
import com.cb.gulimall.product.vo.Catelog2Vo;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedissonClient redisson;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 获取分类json
     */
    @ResponseBody
    @GetMapping("/index/catalog.json")
    public Map<String, List<Catelog2Vo>> catalogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatalogJson();
        return map;
    }

    /**
     * 首页
     *
     * @return
     */
    @GetMapping({"/", "/index", "/index.html"})
    private String index(Model model) {
        List<CategoryEntity> categorys = categoryService.getCategoryLevel1s();

        model.addAttribute("categorys", categorys);
        return "index";
    }

    @GetMapping("/hello")
    @ResponseBody
    public String hello() {
        // 1、获取一把锁，只要锁的名字相同，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        // 2、加锁
        lock.lock(); // 阻塞式等待，默认加锁时间都是30s
        // 1)锁的自动续期，如果业务时间超长，运行期间自动给锁续上新的30s。不用担心业务时间过长，锁自动过期被删除
        // 2）加锁的业务只要运行完成，就不会给当前锁续期，即便不手动解锁，锁默认在30s后自动删除

//        lock.lock(10, TimeUnit.SECONDS); // 10s后自动解锁，自动解锁时间一定要大于业务时间
        // 问题：
        // 1、如果我们设置了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时时间就是我们指定的时间
        // 2、如果我们未指定锁的超时时间，就是用30000L【lockWatchdogTimeout看门狗的默认时间】
        //    只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】，没隔10s就会自动续期为30s
        //    lockWatchdogTimeout【时间】 / 3, 10s

        // 最佳实战
        // 1)lock.lock(30, TimeUnit.SECONDS); 省掉了整个续期操作，手动解锁
        try {
            System.out.println("加锁成功，执行业务。。。" + Thread.currentThread().getId());
            TimeUnit.SECONDS.sleep(30);
        } catch (InterruptedException e) {

        } finally {
            // 3、解锁，假设解锁代码未运行，redisson不会出现死锁
            System.out.println("解锁成功" + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }

    // 保证一定能读取到最新数据，修改期间，写锁是一个排他锁(互斥锁、独享锁)，读锁是一个共享锁
    // 写锁没释放，读锁就一定得等待
    // 读 + 读：相当于无锁，并发读，只会在redis中记录好所有当前的读锁，他们都同时加锁成功
    // 写 + 读：读锁等待写锁释放
    // 写 + 写：阻塞方式
    // 读 + 写：写锁需要等待读锁释放
    // 只要有写的存在，都必须等待
    @GetMapping("/write")
    @ResponseBody
    public String write() {
        String s = "";
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        // 改变数据加写锁，读数据加写锁
        RLock rLock = lock.writeLock();
        rLock.lock();
        try {
            System.out.println("写锁加锁成功 " + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set("writeValue", s);
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {

        } finally {
            System.out.println("写锁释放成功 " + Thread.currentThread().getId());
            rLock.unlock();
        }

        return s;
    }

    @GetMapping("/read")
    @ResponseBody
    public String read() {
        String s = "";
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功 " + Thread.currentThread().getId());
            s = redisTemplate.opsForValue().get("writeValue");
            TimeUnit.SECONDS.sleep(30);
        } catch (Exception e) {

        } finally {
            System.out.println("读锁释放成功 " + Thread.currentThread().getId());
            rLock.unlock();
        }
        return s;
    }

    @GetMapping("/lock/await")
    @ResponseBody
    public String await() throws InterruptedException {
        RCountDownLatch latch = redisson.getCountDownLatch("countDownLatch");
        latch.trySetCount(5);
        latch.await();
        return "放假了";
    }

    @GetMapping("/lock/countDown/{id}")
    @ResponseBody
    public String countDown(@PathVariable("id") Long id) {
        RCountDownLatch latch = redisson.getCountDownLatch("countDownLatch");
        latch.countDown();
        return id + "班的人走了";
    }

    // 可用做分布式限流
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore semaphore = redisson.getSemaphore("semaphore");
        semaphore.acquire(); // 阻塞直到获取到信号量

//        boolean b = semaphore.tryAcquire(); // 尝试获取信号量

        return "acquire ok";
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() {
        RSemaphore semaphore = redisson.getSemaphore("semaphore");
        semaphore.release(); // 释放信号量
        return "release ok";
    }
}
