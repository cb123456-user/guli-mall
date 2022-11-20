package com.cb.gulimall.auth.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class MyControllerViewConfig implements WebMvcConfigurer {

    /**
     * 视图映射，相当于以下controller
     *     @GetMapping("/login.html")
     *     public String login(){
     *         return "login";
     *     }
     *
     *     @GetMapping("/reg.html")
     *     public String reg(){
     *         return "reg";
     *     }
     * @param registry
     */

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/reg.html").setViewName("reg");
    }
}
