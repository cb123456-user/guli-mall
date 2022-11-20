package com.cb.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PurchaseDetailDoneVo {

    private Long itemId;
    private Integer status;
    private String reason;

}
