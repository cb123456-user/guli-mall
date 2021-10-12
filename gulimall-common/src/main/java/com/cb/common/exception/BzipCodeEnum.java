package com.cb.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenbin
 * 错误码,推荐5位编码，10-商品服务 001-参数校验失败
 */

@AllArgsConstructor
@Getter
public enum BzipCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_FAILD_EXCEPTION(10001, "参数校验失败");

    private Integer code;
    private String msg;
}
