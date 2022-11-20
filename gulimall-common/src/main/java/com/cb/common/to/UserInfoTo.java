package com.cb.common.to;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @authoer: chenbin
 * @date: 2022/8/28/028 17:08
 * @description:
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoTo {

    private String userKey;

    private Long userId;

    private Boolean tempUser = false;
}
