package com.cb.gulimall.order.config;

import com.cb.gulimall.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName MyRabbitMqConfig
 * @Description
 * @Author JingXu
 * @Date 2022/12/4 22:47
 */
@Configuration
public class MyRabbitMqConfig {

    @RabbitListener(queues = {"order.release.order.queue"})
    public void receiveMessageTest(Message message, OrderEntity orderEntity, Channel channel) throws IOException {
        System.out.println("接收到过期订单：" + orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

    @Bean
    public Queue orderDelayQueue() {
        // String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 5000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    @Bean
    public Exchange orderEventExchange() {
        // String name, boolean durable, boolean autoDelete, Map<String, Object> arguments
        return new TopicExchange("order-event-exchange", true, false);
    }

    @Bean
    public Binding orderCreateOrderBinding() {
        // String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments
        return new Binding(
                "order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null
        );
    }

    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding(
                "order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null
        );
    }
}
