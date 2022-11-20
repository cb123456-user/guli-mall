package com.cb.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class WareConstant {

    /**
     * 采购单状态
     */
    @AllArgsConstructor
    @Getter
    public enum PurchaseOrderEnum {
        CREATED(0, "新建"),
        ASSIGNEE(1, "已分配"),
        RECIVED(2, "已领取"),
        FINISHED(3, "已完成"),
        HASERROR(4, "有异常");

        private int code;
        private String msg;
    }

    /**
     * 采购需求状态
     */
    @AllArgsConstructor
    @Getter
    public enum PurchaseNeedsEnum {
        CREATED(0, "新建"),
        ASSIGNEE(1, "已分配"),
        BUYING(2, "正在采购"),
        FINISHED(3, "已完成"),
        FAIL(4, "采购失败");

        private int code;
        private String msg;
    }
}
