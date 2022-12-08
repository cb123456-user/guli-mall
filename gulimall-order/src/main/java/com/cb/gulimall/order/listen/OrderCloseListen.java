package com.cb.gulimall.order.listen;

import com.cb.gulimall.order.entity.OrderEntity;
import com.cb.gulimall.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @ClassName OrderListen
 * @Description
 * @Author JingXu
 * @Date 2022/12/7 21:12
 */
@Service
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderCloseListen {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void receiveMessageTest(Message message, OrderEntity orderEntity, Channel channel) throws IOException {
        System.out.println("收到过期的订单信息，准备关闭订单" + orderEntity.getOrderSn());
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
