package com.cb.common.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class MemberLoginVo {

    private String loginacct;

    private String password;
}
