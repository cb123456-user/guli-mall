package com.cb.gulimall.auth.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginAcctVo {

    private String loginacct;

    private String password;
}
