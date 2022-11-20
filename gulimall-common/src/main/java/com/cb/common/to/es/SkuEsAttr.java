package com.cb.common.to.es;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SkuEsAttr {

    private Long attrId;

    private String attrName;

    private String attrValue;
}
