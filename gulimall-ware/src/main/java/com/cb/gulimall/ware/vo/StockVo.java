package com.cb.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class StockVo {

    private Long skuId;
    private Long wareId;
    private Integer stock;
}
