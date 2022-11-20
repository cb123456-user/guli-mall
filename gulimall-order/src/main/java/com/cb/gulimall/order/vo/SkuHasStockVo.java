package com.cb.gulimall.order.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SkuHasStockVo {

    private Long skuId;

    private Boolean hasStock;
}
