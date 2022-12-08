package com.cb.gulimall.order.web;

import com.alibaba.fastjson.JSON;
import com.cb.common.constant.RedirectConstant;
import com.cb.common.exception.NoStockException;
import com.cb.gulimall.order.service.OrderService;
import com.cb.gulimall.order.vo.OrderConfirmVo;
import com.cb.gulimall.order.vo.OrderSubmitVo;
import com.cb.gulimall.order.vo.SubmitOrderResponseVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;


/**
 * @authoer: chenbin
 * @date: 2022/11/12/012 20:46
 * @description:
 */
@Slf4j
@Controller
public class OrderWebController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/{page}.html")
    public String page(@PathVariable("page") String page) {
        return page;
    }


    @GetMapping("/toTrade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {
        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();
        model.addAttribute("orderConfirmData", orderConfirmVo);
        return "confirm";
    }

    /**
     * 提交订单
     * 成功：跳转到支付页面
     * 失败:重定向到确认页面
     */
    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        try {
            SubmitOrderResponseVo responseVo = orderService.orderSubmit(orderSubmitVo);

            Integer code = responseVo.getCode();

            if (code == 0) {
                model.addAttribute("submitOrderResp", responseVo);
                return "pay";
            }

            String msg = "下单失败";
            switch (responseVo.getCode()) {
                case 1:
                    msg += "订单信息过期,请重新提交";
                    break;
                case 2:
                    msg += "订单商品价格发送变化,请确认后再次提交";
                    break;
            }
            redirectAttributes.addFlashAttribute("msg", msg);

            return RedirectConstant.ORDER_CONFIRM_URL;

        } catch (Exception e) {
            log.error("submitOrder execute fail, param {}, error {}", JSON.toJSONString(orderSubmitVo), e);
            redirectAttributes.addFlashAttribute("msg", "下单失败:" + e.getMessage());
            return RedirectConstant.ORDER_CONFIRM_URL;
        }
    }
}
