package com.cb.gulimall.order;

import com.cb.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

@Slf4j
@SpringBootTest
class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void createExchange() {
        // DirectExchange(String name, boolean durable, boolean autoDelete, Map<String, Object> arguments)
        DirectExchange directExchange = new DirectExchange("exchange.java.direct", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("createExchange【{}】成功", "exchange.java.direct");
    }

    @Test
    public void createQueue() {
        // public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments)
        // exclusive 是否排他：false
        Queue queue = new Queue("rabbitmq.java.queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("createQueue【{}】成功", "rabbitmq.java.queue");
    }

    @Test
    public void bindIng() {
        // Binding(String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments)
        // destination 目标：queue / exchange
        // destinationType 类型：queue / exchange
        Binding binding = new Binding("rabbitmq.java.queue", Binding.DestinationType.QUEUE, "exchange.java.direct", "hello", null);
        amqpAdmin.declareBinding(binding);
        log.info("exchange【{}】,queue【{}】,routing key【{}】bindIng 成功", "exchange.java.direct", "rabbitmq.java.queue", "hello");
    }

    @Test
    public void sendMessage(){

        /**
         * RabbitAutoConfiguration自动配置时，如果容器中没有 MessageConverter，rabbitTemplate就使用默认的SimpleMessageConverter，对象会序列化
         * 发送消息如果是对象，会使用序列化机制，将对象写出去
         * fromMessage方法 SerializationUtils.deserialize
         *
         * 如果对象想输出为json格式，需要往容器中注入 Jackson2JsonMessageConverter
         */
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(4L);
        reasonEntity.setName("hello");

        // convertAndSend(String exchange, String routingKey, Object message, CorrelationData correlationData)
        // CorrelationData 消息的唯一标识
        rabbitTemplate.convertAndSend("exchange.java.direct", "hello", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
        log.info("send message【{}】 success", reasonEntity);
    }

}
