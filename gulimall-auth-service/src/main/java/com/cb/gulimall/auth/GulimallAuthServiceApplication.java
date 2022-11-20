package com.cb.gulimall.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 *	核心原理：
 *  @EnableRedisHttpSession 导入 RedisHttpSessionConfiguration 配置
 *	    1、给容器添加了一个组件RedisIndexedSessionRepository
 *	        SessionRepository => RedisIndexedSessionRepository => redis操作session,相当于是redis操作session的dao[增删改查]
 *		2、继承了SpringHttpSessionConfiguration配置
 *	    	2.1 Cookie进行了初始化，初始化为自定义cookie
 *			2.2 SessionRepositoryFilter => Filter session存储过滤器，每个请求都要经过filter
 *		        2.2.1 创建时自动从容器中获取SessionRepository
 *		        2.2.2 将原生的request、response包装成 SessionRepositoryRequestWrapper、SessionRepositoryResponseWrapper
 *		        2.2.3 以后获取session。 request.getSession()
 *		        2.2.4 wrappedRequest.getSession() => SessionRepository获取
 * 装饰者模式
 * 网页不关闭，请求后会对redis数据自动延期
 */
// 启用springsession
@EnableRedisHttpSession
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
public class GulimallAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallAuthServiceApplication.class, args);
    }

}
