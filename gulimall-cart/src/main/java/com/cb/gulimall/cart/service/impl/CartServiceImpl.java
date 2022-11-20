package com.cb.gulimall.cart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cb.common.constant.CartConstant;
import com.cb.common.to.UserInfoTo;
import com.cb.common.utils.R;
import com.cb.gulimall.cart.feign.ProductFeignService;
import com.cb.gulimall.cart.interceptor.CartInterceptor;
import com.cb.gulimall.cart.service.CartService;
import com.cb.gulimall.cart.vo.Cart;
import com.cb.gulimall.cart.vo.CartItem;
import com.cb.gulimall.cart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @authoer: chenbin
 * @date: 2022/8/28/028 21:24
 * @description:
 */
@Service
@Slf4j
public class CartServiceImpl implements CartService {

    @Autowired
    public StringRedisTemplate redisTemplate;

    @Autowired
    private ProductFeignService productFeignService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        // 获取绑定key的hash
        BoundHashOperations<String, Object, Object> cartItemops = getCartItemOps();

        String itemObj = (String) cartItemops.get(skuId.toString());

        // 购物项不存在，第一次添加
        if (StringUtils.isEmpty(itemObj)) {
            CartItem cartItem = new CartItem();

            CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(() -> {
                // 根据skuId 查询sku信息
                R skuInfoResult = productFeignService.info(skuId);
                SkuInfoVo skuInfoVo = skuInfoResult.getData("skuInfo", new TypeReference<SkuInfoVo>() {
                });
                cartItem.setSkuId(skuId);
                cartItem.setCount(num);
                cartItem.setCheck(true);
                cartItem.setImage(skuInfoVo.getSkuDefaultImg());
                cartItem.setTitle(skuInfoVo.getSkuTitle());
                cartItem.setPrice(skuInfoVo.getPrice());
            }, threadPoolExecutor);


            CompletableFuture<Void> saleAttrValueTask = CompletableFuture.runAsync(() -> {
                // 根据skuId 查询sku销售属性
                List<String> saleAttrValues = productFeignService.skuSaleAttrValues(skuId);
                cartItem.setSkuAttrValues(saleAttrValues);
            }, threadPoolExecutor);

            CompletableFuture.allOf(skuInfoTask, saleAttrValueTask).get();

            cartItemops.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }

        // 购物项存在，数量相加
        CartItem cartItem = JSON.parseObject(itemObj, CartItem.class);
        cartItem.setCount(cartItem.getCount() + num);
        cartItemops.put(skuId.toString(), JSON.toJSONString(cartItem));

        return cartItem;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartItemOps = getCartItemOps();
        String o = (String) cartItemOps.get(skuId.toString());
        return JSON.parseObject(o, CartItem.class);
    }

    /**
     * 用户未登录：
     *  Redis获取购物车数据
     * 用户登录：
     *  redi获取未登录数据，如果存在，需要数据合并
     *  再查登录后的购物车数据
     * @return
     */
    @Override
    public Cart getCart() throws ExecutionException, InterruptedException {
        Cart cart = new Cart();

        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();

        // 获取临时购物车
        String tempKey = CartConstant.CART_KEY_PREFIX + userInfoTo.getUserKey();
        List<CartItem> tempItems = getcartItems(tempKey);

        // 用户未登录，返回临时购物车数据
        if(userInfoTo.getUserId() == null){
            cart.setItems(tempItems);
            return cart;
        }

        // 用户登录，合并临时购物车
        if(tempItems != null && tempItems.size() > 0){
            for (CartItem cartItem : tempItems) {
                addToCart(cartItem.getSkuId(), cartItem.getCount());
            }
            // 合并临时购物车后，删除临时购物车数据
            clearCart(tempKey);
        }

        // 获取用户购物车数据
        String cartKey = CartConstant.CART_KEY_PREFIX + userInfoTo.getUserId();
        List<CartItem> cartItems = getcartItems(cartKey);
        cart.setItems(cartItems);

        return cart;

    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(1==check);
        BoundHashOperations<String, Object, Object> cartItemOps = getCartItemOps();
        cartItemOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        BoundHashOperations<String, Object, Object> cartItemOps = getCartItemOps();
        cartItemOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartItemOps = getCartItemOps();
        cartItemOps.delete(skuId.toString());
    }

    @Override
    public void clearCart(String cartKey) {
        redisTemplate.delete(cartKey);
    }

    @Override
    public List<CartItem> memberCartItems() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();
        String cartKey = CartConstant.CART_KEY_PREFIX + userInfoTo.getUserId();
        List<CartItem> cartItems = getcartItems(cartKey);
        List<CartItem> itemList = cartItems.stream().filter(it -> it.getCheck()).map(it -> {
            // 实时价格，不是加入购物车是的价格
            R response = productFeignService.getPrice(it.getSkuId());
            String data = (String) response.get("data");
            it.setPrice(new BigDecimal(data));
            return it;
        }).collect(Collectors.toList());
        return itemList;
    }

    private BoundHashOperations<String, Object, Object> getCartItemOps() {
        UserInfoTo userInfoTo = CartInterceptor.userInfoToThreadLocal.get();
        String cartKey = CartConstant.CART_KEY_PREFIX + (userInfoTo.getUserId() == null ? userInfoTo.getUserKey() : userInfoTo.getUserId().toString());
        return redisTemplate.boundHashOps(cartKey);
    }

    private List<CartItem> getcartItems(String cartKey){
        BoundHashOperations<String, Object, Object> cartItemOps = redisTemplate.boundHashOps(cartKey);
        List<Object> values = cartItemOps.values();
        if(values != null && values.size() > 0){
            return values.stream().map(it->{
                String str = (String) it;
                return JSON.parseObject(str, CartItem.class);
            }).collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
