package com.cb.gulimall.cart.service;

import com.cb.gulimall.cart.vo.Cart;
import com.cb.gulimall.cart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @authoer: chenbin
 * @date: 2022/8/28/028 21:23
 * @description:
 */
public interface CartService {
    CartItem addToCart(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItem getCartItem(Long skuId);

    Cart getCart() throws ExecutionException, InterruptedException;

    void checkItem(Long skuId, Integer check);

    void countItem(Long skuId, Integer num);

    void deleteItem(Long skuId);

    void clearCart(String cartKey);

    List<CartItem> memberCartItems();
}
