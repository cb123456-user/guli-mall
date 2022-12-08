package com.cb.gulimall.ware.listen;

import com.cb.common.to.OrderTo;
import com.cb.common.to.StockLockedTo;
import com.cb.gulimall.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @ClassName RelaseStockService
 * @Description
 * @Author JingXu
 * @Date 2022/12/7 19:48
 */
@Service
@RabbitListener(queues = {"stock.release.stock.queue"})
public class ReleaseStockService {

    @Autowired
    private WareSkuService wareSkuService;

    @RabbitHandler
    public void stockRelease(StockLockedTo stockLockedTo, Message message, Channel channel) throws IOException {

        System.out.println("收到解锁库存消息，orderSn =" + stockLockedTo.getOrderSn() + ", taskId = " + stockLockedTo.getTaskId());
        try {
            wareSkuService.stockRelease(stockLockedTo);
            // ack确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 拒绝，重新入队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void stockRelease(OrderTo orderTo, Message message, Channel channel) throws IOException {

        System.out.println("收到订单关闭消息，解锁库存，orderSn =" + orderTo.getOrderSn());
        try {
            wareSkuService.stockRelease(orderTo);
            // ack确认
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            // 拒绝，重新入队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
