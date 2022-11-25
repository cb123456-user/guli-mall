package com.cb.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @authoer: chenbin
 * @date: 2022/11/12/012 23:15
 * @description:
 */
@Data
@Accessors(chain = true)
public class OrderConfirmVo {

    /**
     * 用户收货地址
     */
    public List<MemberAddressVo> address;

    /**
     * 商品列表
     */
    public List<OrderItemVo> items;

    /**
     * 积分
     */
    private Integer integration;

    /**
     * 发票信息
     */

    /**
     * 优惠券信息
     */

    /**
     * 总数
     */
    private Integer count;

    /**
     * 总价
     */
    private BigDecimal totalPrice;

    /**
     * 实际支付
     */
    private BigDecimal payPrice;

    /**
     * 优惠
     */
    private BigDecimal reducePrice;

    /**
     * 库存
     * @return
     */
    private Map<Long, Boolean> stocks;

    /**
     * 订单提交幂等性标识
     */
    private String orderToken;

    public Integer getCount() {
        int num = 0;
        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemVo item : items) {
                num += item.getCount();
            }
        }
        return num;
    }

    public BigDecimal getTotalPrice() {

        BigDecimal total = new BigDecimal("0");

        if (!CollectionUtils.isEmpty(items)) {
            for (OrderItemVo item : items) {
                total = total.add(item.getPrice().multiply(new BigDecimal("" + item.getCount())));
            }
        }

        return total;
    }

    public BigDecimal getPayPrice() {
        return getTotalPrice();
    }

}
