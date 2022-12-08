package com.cb.common.to;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @ClassName WareSkuLockVo
 * @Description
 * @Author JingXu
 * @Date 2022/12/7 20:14
 */
@Data
@Accessors
public class StockLockedTo {
    private Long id;
    /**
     * sku_id
     */
    private Long skuId;
    /**
     * sku_name
     */
    private String skuName;
    /**
     * 购买个数
     */
    private Integer skuNum;
    /**
     * 工作单id
     */
    private Long taskId;
    /**
     * 仓库id
     */
    private Long wareId;
    /**
     * 1-已锁定  2-已解锁  3-扣减
     */
    private Integer lockStatus;

    /**
     * 订单号
     */
    private String orderSn;
}
