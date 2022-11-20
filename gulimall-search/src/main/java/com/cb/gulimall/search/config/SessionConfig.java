package com.cb.gulimall.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * 设置Session作用域、自定义cookie序列化机制
 */
@Configuration
public class SessionConfig {

    /**
     * 自定义cookie
     *
     * @return
     */
    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        // 设置cookie名
        serializer.setCookieName("GULISESSION");
        // 扩大作用域
        serializer.setDomainName("gulimall.com");
        return serializer;
    }

    /**
     * json序列化，不使用jdk序列化
     *
     * @return
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer();
    }

}
