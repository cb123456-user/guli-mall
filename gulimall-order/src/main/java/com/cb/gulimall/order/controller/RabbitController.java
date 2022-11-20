package com.cb.gulimall.order.controller;

import com.cb.gulimall.order.entity.OrderReturnReasonEntity;
import com.cb.gulimall.order.entity.RefundInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * @authoer: chenbin
 * @date: 2022/11/2/002 19:59
 * @description:
 */
@Slf4j
@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/sendMessage")
    public String sendMesage(@RequestParam(value = "num", required = false, defaultValue = "10") Integer num) {

        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setId(4L);
        reasonEntity.setCreateTime(new Date());

        RefundInfoEntity refundInfoEntity = new RefundInfoEntity();
        refundInfoEntity.setId(100L);

        for (int i = 0; i < num; i++) {

            if (i % 2 == 0) {
                reasonEntity.setName("hello-" + i);
                rabbitTemplate.convertAndSend("exchange.java.direct", "hello", reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
                log.info("send message【{}】 success", reasonEntity);
            } else{
                refundInfoEntity.setRefundContent("xxxxx-" + i);
                rabbitTemplate.convertAndSend("exchange.java.direct", "hello", refundInfoEntity, new CorrelationData(UUID.randomUUID().toString()));
                log.info("send message【{}】 success", refundInfoEntity);
            }


        }

        return "ok";
    }
}
