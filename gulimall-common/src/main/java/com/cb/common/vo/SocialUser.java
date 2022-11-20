package com.cb.common.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SocialUser {

    /**
     * 用户授权的唯一票据
     */
    private String accessToken;

    /**
     * access_token的生命周期，单位是秒数。
     */
    private String expiresIn;

    /**
     * access_token的生命周期（该参数即将废弃，开发者请使用expires_in）
     */
    private String remindIn;

    /**
     * 授权用户的UID
     */
    private String uid;

    private String isrealname;

}
