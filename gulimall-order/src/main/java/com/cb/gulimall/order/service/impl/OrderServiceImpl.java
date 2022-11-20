package com.cb.gulimall.order.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.cb.common.utils.R;
import com.cb.common.vo.MemberInfoVo;
import com.cb.gulimall.order.feign.CartFeignService;
import com.cb.gulimall.order.feign.MemeberFeignService;
import com.cb.gulimall.order.feign.WareFeignService;
import com.cb.gulimall.order.interceptor.OrderLoginInterceptor;
import com.cb.gulimall.order.vo.MemberAddressVo;
import com.cb.gulimall.order.vo.OrderConfirmVo;
import com.cb.gulimall.order.vo.OrderItemVo;
import com.cb.gulimall.order.vo.SkuHasStockVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.order.dao.OrderDao;
import com.cb.gulimall.order.entity.OrderEntity;
import com.cb.gulimall.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private MemeberFeignService memeberFeignService;

    @Autowired
    private CartFeignService cartFeignService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 异步线程feign header丢失
     *  异步线程获取不到主线程的header信息，需要手动设置header信息
     */
    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();

        MemberInfoVo memberInfoVo = OrderLoginInterceptor.threadLocal.get();

        System.out.println("主线程：" + Thread.currentThread().getId());

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        // 查询收货地址信息
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            System.out.println("addressFuture：" + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressVos = memeberFeignService.memberArrress(memberInfoVo.getId());
            orderConfirmVo.setAddress(addressVos);
        }, threadPoolExecutor);

        // 查询购物项信息
        CompletableFuture<Void> cartItemFuture = CompletableFuture.runAsync(() -> {
            System.out.println("cartItemFuture：" + Thread.currentThread().getId());
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> orderItemVos = cartFeignService.memberCartItems();
            orderConfirmVo.setItems(orderItemVos);
        }, threadPoolExecutor).thenRunAsync(() -> {
            List<Long> skuIds = orderConfirmVo.getItems().stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R response = wareFeignService.hashstock(skuIds);
            List<SkuHasStockVo> stockVos = response.getData(new TypeReference<List<SkuHasStockVo>>() {
            });
            Map<Long, Boolean> map = stockVos.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
            orderConfirmVo.setStocks(map);
        }, threadPoolExecutor);

        // 查询积分信息
        Integer integration = memberInfoVo.getIntegration();
        orderConfirmVo.setIntegration(integration);

        CompletableFuture.allOf(addressFuture, cartItemFuture).get();

        return orderConfirmVo;
    }

}