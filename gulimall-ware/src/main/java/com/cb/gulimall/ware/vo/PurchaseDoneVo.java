package com.cb.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class PurchaseDoneVo {

    private Long id;

    private List<PurchaseDetailDoneVo> items;
}
