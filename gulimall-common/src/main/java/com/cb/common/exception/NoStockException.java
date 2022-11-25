package com.cb.common.exception;

import lombok.Data;

/**
 * @author chenbin
 * @date 2022/11/25 10:29
 */
@Data
public class NoStockException extends RuntimeException {

    private Long skuId;

    public NoStockException(Long skuId) {
        super("商品:id" + skuId + "库存不足");
    }

    public NoStockException(String msg) {
        super(msg);
    }
}
