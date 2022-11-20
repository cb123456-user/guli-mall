package com.cb.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class MergeVo {

    /**
     * purchaseId: 1, //整单id
     *   items:[1,2,3,4] //合并项集合
      */
    private Long purchaseId;
    private List<Long> items;
}
