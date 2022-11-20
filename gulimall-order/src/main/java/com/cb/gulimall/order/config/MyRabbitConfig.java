package com.cb.gulimall.order.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @authoer: chenbin
 * @date: 2022/11/2/002 19:52
 * @description:
 */
@Configuration
@Slf4j
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * MyRabbitConfig对象创建完成后执行initRabbit方法
     */
    @PostConstruct
    public void initRabbit() {
        /**
         * 	设置确认回调
         *  correlationData: 消息的唯一id
         *  ack： 消息是否成功收到
         * 	cause：失败的原因
         */
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            System.out.println("confirmCallback: " + correlationData + "==>ack: " + ack + "==>cause： " + cause);
        });

        /**
         * 设置消息抵达队列回调：可以很明确的知道那些消息失败了
         *
         * message: 投递失败的消息详细信息
         * replyCode: 回复的状态码
         * replyText: 回复的文本内容
         * exchange: 当时这个发送给那个交换机
         * routerKey: 当时这个消息用那个路由键
         */
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.err.println("returnCallback [" + message + "]" + "==>replyCode: " + replyCode + "==>replyText:" + replyText + "==>exchange:" + exchange + "==>routingKey:" + routingKey);
        });
    }
}
