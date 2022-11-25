package com.cb.gulimall.order.vo;

import com.cb.gulimall.order.entity.OrderEntity;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author chenbin
 * @date 2022/11/24 19:33
 */
@Data
@Accessors(chain = true)
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /**
     * 0-成功，否则失败
     */
    private Integer code;

}
