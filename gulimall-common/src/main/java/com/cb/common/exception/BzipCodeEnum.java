package com.cb.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author chenbin
 * 错误码,推荐5位编码，如：10-商品服务 001-参数校验失败
 *  10 通用
 *      001 参数格式校验
 *      002 短信验证码频率太高
 *  11 商品
 *  12 订单
 *  13 购物车
 *  14 物流
 *  15用户
 *  23 仓库
 */

@AllArgsConstructor
@Getter
public enum BzipCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VALID_FAILD_EXCEPTION(10001, "参数校验失败"),
    DATA_EXCEPTION(10002, "数据异常"),
    SMS_CODE_EXCEPTION(10002, "验证码获取频繁太高，稍后再试"),

    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),

    USER_EXIST_EXCEPTION(15001, "用户存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号存在"),
    LOGIN_VALID_EXCEPTION(15003, "用户密码错误"),

    NO_STOCK_EXCEPTION(23001, "商品库存不足")
    ;


    private Integer code;
    private String msg;
}
