package com.cb.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @author chenbin
 * @date 2022/11/24 19:15
 */
@Data
@Accessors(chain = true)
public class OrderSubmitVo {

    /**
     * 收货地址id
     */
    private Long addrId;

    /**
     * 订单来源
     */
    private Integer SourceType = 0;

    /**
     * 配送信息
     */

    /**
     * 购物项，可以直接从购物车查询，不用传递
     */

    /**
     * 发票信息
     */

    /**
     * 优惠信息
     */

    /**
     * 应付价格
     */
    private BigDecimal payPrice;

    /**
     * 防重token
     */
    private String orderToken;

    /**
     * 备注
     */
    private String note;

}
