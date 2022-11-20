package com.cb.gulimall.cart.controller;

import com.cb.common.constant.CartConstant;
import com.cb.gulimall.cart.service.CartService;
import com.cb.gulimall.cart.vo.Cart;
import com.cb.gulimall.cart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @authoer: chenbin
 * @date: 2022/8/28/028 16:55
 * @description:
 */
@Controller
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/cartItems")
    @ResponseBody
    public List<CartItem> memberCartItems(){
        return cartService.memberCartItems();
    }


    /**
     * 去购物车页面的请求
     * 浏览器有一个cookie:user-key 标识用户的身份，一个月过期
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份:
     * 浏览器以后保存，每次访问都会带上这个cookie；
     * <p>
     * 登录：session有
     * 没登录：按照cookie里面带来user-key来做
     * 第一次，如果没有临时用户，自动创建一个临时用户
     *
     * @return
     */
    @GetMapping("/cart.html")
    public String cartHtml(Model model) throws ExecutionException, InterruptedException {

        Cart cart = cartService.getCart();

        model.addAttribute("cart", cart);

        return "cartList";
    }

    /**
     * redirectAttributes:
     *  addFlashAttribut 将数据放到session,页面可以取出，只能取一次
     *  addAttribute 会自动将数据添加到url后面
     */
    @GetMapping("/addToCart")
    public String addToCart(
            @RequestParam("skuId") Long skuId,
            @RequestParam("num") Integer num,
            Model model,
            RedirectAttributes redirectAttributes
    ) throws ExecutionException, InterruptedException {
//
        CartItem cartItem = cartService.addToCart(skuId, num);

//        // 转发存在重复提交问题,通过重定向解决
//        model.addAttribute("item", cartItem);
//        return "success";

        redirectAttributes.addAttribute("skuId", skuId);
        return CartConstant.ADD_CART_SUCCESS_HTML_URL;

    }

    @GetMapping("/addCartSuccess")
    public String addCartSuccess(@RequestParam("skuId") Long skuId, Model model) {
        CartItem cartItem = cartService.getCartItem(skuId);

        model.addAttribute("item", cartItem);

        return "success";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);

        return CartConstant.CART_lIST_HTML_URL;
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);

        return CartConstant.CART_lIST_HTML_URL;
    }

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);

        return CartConstant.CART_lIST_HTML_URL;
    }

}
