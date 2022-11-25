package com.cb.gulimall.ware.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>Title: OrderItemVo</p>
 * Description：
 * date：2020/6/30 16:38
 */
@Data
public class OrderItemVo {

    private Long skuId;

    private Boolean check = true;

    private String title;

    private String image;

    private List<String> skuAttrValues;

    private BigDecimal price;

    private Integer count;

    private BigDecimal totalPrice;

    /**
     * 是否有货
     */
	private boolean hasStock = true;

    /**
     * 重量
     */
    private BigDecimal weight;
}
