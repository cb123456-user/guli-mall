package com.cb.gulimall.order.config;

import com.cb.gulimall.order.interceptor.OrderLoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @authoer: chenbin
 * @date: 2022/11/12/012 23:12
 * @description:
 */
@Configuration
public class OrderWebConfiguration implements WebMvcConfigurer {

    @Autowired
    private OrderLoginInterceptor orderLoginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(orderLoginInterceptor).addPathPatterns("/**").excludePathPatterns("/order/order/ignore/**");
    }
}
