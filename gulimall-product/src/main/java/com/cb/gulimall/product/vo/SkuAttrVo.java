package com.cb.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SkuAttrVo {

    private Long attrId;

    private String attrName;

    private List<SkuAttrValueVo> attrValues;
}
