package com.cb.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedisConfig {

    @Bean(destroyMethod="shutdown")
    public RedissonClient redisson() {
        Config config = new Config();
        // Redis url should start with redis:// or rediss:// (for SSL connection)
        config.useSingleServer().setAddress("redis://169.254.37.10:6379");
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
