package com.cb.gulimall.ware.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * @authoer: chenbin
 * @date: 2022/11/13/013 22:19
 * @description:
 */
@Data
@Accessors(chain = true)
public class FareVo {

    private BigDecimal fare;

    private MemberAddressVo memberAddressVo;
}
