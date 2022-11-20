package com.cb.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties(prefix = "thread")
public class ThreadPoolProperty {

    private Integer minSize;
    private Integer maxSize;
    private Integer aliveTime;
}
