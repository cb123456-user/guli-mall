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

    @AllArgsConstructor
    @Getter
    public enum StatusTypeEnum {
        PRODUCT_NEW(0, "新建"),
        PRODUCT_UP(1, "上架"),
        PRODUCT_DOWN(2, "下架");

        private int code;
        private String msg;
    }
}
