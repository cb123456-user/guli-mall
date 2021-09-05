package com.cb.gulimall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * 解决跨域问题
 * springboot自带跨域过滤器 CorsWebFilter
 * gateway网关需要选reactive的包
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration configuration = new CorsConfiguration();

        // 允许所有header
        configuration.addAllowedHeader("*");
        // 允许所有请求方式
        configuration.addAllowedMethod("*");
        // 允许所有来源
        configuration.addAllowedOrigin("*");
        // 允许携带cookie
        configuration.setAllowCredentials(true);

        // 对所有请求生效
        source.registerCorsConfiguration("/**", configuration);

        return new CorsWebFilter(source);
    }
}
