package com.cb.gulimall.cart.vo;

import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * @authoer: chenbin
 * @date: 2022/8/28/028 16:32
 * @description:
 */
@NoArgsConstructor
@ToString
public class Cart {

    /**
     * 购物车子项信息
     */
    private List<CartItem> items;

    /**
     * 商品总数
     */
    private Integer countNum;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 优惠金额
     */
    private BigDecimal reduce = new BigDecimal("0.00");

    /**
     * 商品类型数
     */
    private Integer countType;

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Integer getCountNum() {
        int count = 0;
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                count += item.getCount();
            }
        }
        return count;
    }

    public void setCountNum(Integer countNum) {
        this.countNum = countNum;
    }

    public BigDecimal getTotalAmount() {
        BigDecimal amount = new BigDecimal("0");
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                if(item.getCheck()){
                    BigDecimal totalPrice = item.getTotalPrice();
                    amount = amount.add(totalPrice);
                }
            }
        }
        return amount.subtract(this.getReduce());
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getReduce() {
        return reduce;
    }

    public void setReduce(BigDecimal reduce) {
        this.reduce = reduce;
    }

    public Integer getCountType() {
        int count = 0;
        if(this.items != null && this.items.size() > 0){
            for (CartItem item : this.items) {
                count += 1;
            }
        }
        return count;
    }

    public void setCountType(Integer countType) {
        this.countType = countType;
    }
}
