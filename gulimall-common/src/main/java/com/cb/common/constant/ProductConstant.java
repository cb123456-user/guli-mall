package com.cb.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ProductConstant {

    @AllArgsConstructor
    @Getter
    public enum attrTypeEnum {
        ATTR_TYPE_BASE(1, "基础属性"),
        ATTR_TYPE_SALE(0, "销售属性");

        private int code;
        private String msg;
    }
}
