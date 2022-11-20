package com.cb.common.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MemberRegistVo {

    private String userName;
    private String password;
    private String phone;
}
