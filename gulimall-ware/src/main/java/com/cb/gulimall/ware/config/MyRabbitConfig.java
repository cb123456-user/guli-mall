package com.cb.gulimall.ware.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashMap;

/**
 * @author: chenbin
 * @date: 2022/11/2/002 19:52
 * @description:
 */
@Configuration
@Slf4j
public class MyRabbitConfig {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 序列化方式
     * @return
     */
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

    @Bean
    public Exchange exchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 普通队列
     *
     * @return
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        //String name, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        Queue queue = new Queue("stock.release.stock.queue", true, false, false);
        return queue;
    }


    /**
     * 延迟队列
     *
     * @return
     */
    @Bean
    public Queue stockDelay() {

        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 订单关闭1分钟，解库存消息过期时间 2分钟
        arguments.put("x-message-ttl", 120000);

        return new Queue("stock.delay.queue", true, false, false, arguments);
    }


    /**
     * 交换机与普通队列绑定
     *
     * @return
     */
    @Bean
    public Binding stockReleaseBinding() {
        //String destination, DestinationType destinationType, String exchange, String routingKey,
        // 			Map<String, Object> arguments
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",
                null);
    }


    /**
     * 交换机与延迟队列绑定
     *
     * @return
     */
    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.locked",
                null);
    }
}
