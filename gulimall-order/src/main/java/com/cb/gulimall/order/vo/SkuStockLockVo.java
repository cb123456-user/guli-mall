package com.cb.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;


/**
 * @author chenbin
 * @date 2022/11/25 9:19
 */
@Data
@Accessors(chain = true)
public class SkuStockLockVo {

    /**
     * 订单号
     */
    private String orderSn;

    /**
     * 要锁住的所有库存信息
     */
    private List<OrderItemVo> locks;
}
