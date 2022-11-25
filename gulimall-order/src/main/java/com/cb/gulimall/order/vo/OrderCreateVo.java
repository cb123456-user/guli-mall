package com.cb.gulimall.order.vo;

import com.cb.gulimall.order.entity.OrderEntity;
import com.cb.gulimall.order.entity.OrderItemEntity;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author chenbin
 * @date 2022/11/24 19:57
 */
@Data
@Accessors(chain = true)
public class OrderCreateVo {

    private OrderEntity orderEntity;

    private List<OrderItemEntity> orderItems;
}
