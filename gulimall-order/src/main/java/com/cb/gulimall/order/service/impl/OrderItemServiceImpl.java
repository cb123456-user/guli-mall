package com.cb.gulimall.order.service.impl;

import com.cb.gulimall.order.entity.OrderReturnReasonEntity;
import com.cb.gulimall.order.entity.RefundInfoEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.order.dao.OrderItemDao;
import com.cb.gulimall.order.entity.OrderItemEntity;
import com.cb.gulimall.order.service.OrderItemService;


@RabbitListener(queues = {"rabbitmq.java.queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 消费注解
     *
     * @RabbitListener：可标注在类、方法上，指定监听的队列，queues可配置多个 标注在类上，表示是消息消费类
     * @RabbitHandler：可标注在方法上 标注在方法上，消费不同类型消息
     * <p>
     * <p>
     * 接收参数：
     * Message: 原生的message信息
     * (Body:'{"id":4,"name":"hello-0","sort":null,"status":null,"createTime":1667391274331}' MessageProperties [headers={__TypeId__=com.cb.gulimall.order.entity.OrderReturnReasonEntity}, contentType=application/json, contentEncoding=UTF-8, contentLength=0, receivedDeliveryMode=PERSISTENT, priority=0, redelivered=false, receivedExchange=exchange.java.direct, receivedRoutingKey=hello, deliveryTag=1, consumerTag=amq.ctag-W2PgSQPn6V_voAjFlZOvLA, consumerQueue=rabbitmq.java.queue])
     * body 消息体 byte[]
     * MessageProperties 消息投
     * <p>
     * content：具体的消息内容，可以指定指定内容的类型接收消息
     * <p>
     * Channel：通道
     * 消费消息时ack机制使用，告诉消息服务器消费信息
     * <p>
     * <p>
     * 消息处理完成后才会消费下一条消息，并且一条消息只会被消费一次 （dircet交换机）
     *
     *
     */
//    @RabbitListener(queues={"rabbitmq.java.queue"})
    @RabbitHandler
    public void receiveMessageA(Message message, OrderReturnReasonEntity content, Channel channel) {
        System.out.println("接受到消息: " + message + "==>内容：" + content);

        //deliveryTag: 一个数字 通道内自增
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==>" + deliveryTag);
        try {
            if (deliveryTag % 2 == 0) {
                // 只签收当前货物 不批量签收
                channel.basicAck(deliveryTag, false);
                System.out.println("签收了消息。。" + deliveryTag);
            } else {
                // deliveryTag: 货物的标签  	multiple: 是否批量拒收 requeue: 是否重新入队
                channel.basicNack(deliveryTag, false, false);
                // deliveryTag: 货物的标签  requeue: 是否重新入队
//                channel.basicReject(deliveryTag, false);
                System.out.println("未签收消息。。" + deliveryTag);
            }

        } catch (IOException e) {
            System.err.println("网络中断");
        }
    }

    @RabbitHandler
    public void receiveMessageB(Message message, RefundInfoEntity content, Channel channel) {
        System.out.println("接受到消息: " + message + "==>内容：" + content);

        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            System.err.println("网络中断");
        }
    }

}