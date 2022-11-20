package com.cb.gulimall.member;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.Md5Crypt;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


//@SpringBootTest
class GulimallMemberApplicationTests {

    @Test
    void contextLoads() {

        // md5加密 密文长度一致 容易计算MD5 不可逆
        // org.apache.commons.codec.digest.DigestUtils
        // 202cb962ac59075b964b07152d234b70
        // 抗修改性 彩虹表破解 123->202cb962ac59075b964b07152d234b70
        String md5Hex = DigestUtils.md5Hex("123");
        System.out.println(md5Hex);

        // MD5盐值加密 随机值：$1$+8位字符
        // $1$12345678$a4ge4d5iJ5vwvbFS88TEN0 数据库需要维护一个sign
        String md5Crypt = Md5Crypt.md5Crypt("123456".getBytes(), "$1$12345678");
        System.out.println(md5Crypt);

        // spring密码编码器
        // $2a$10$L.mLK4f92t3CtyFvkhVAfOjXUgcTEyrG0sR4b9m1Go.engGXgNAqO 123456
        // $2a$10$4brI3wc4xRnPOrDjn4bZAOv2GsC3kSzyP4MeDtd4y9OtmFlH/nwWS 123456
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode("123456");
        System.out.println(encode);

        boolean matches1 = passwordEncoder.matches("123456", "$2a$10$L.mLK4f92t3CtyFvkhVAfOjXUgcTEyrG0sR4b9m1Go.engGXgNAqO");
        System.out.println(matches1);

        boolean matches2 = passwordEncoder.matches("123456", "$2a$10$4brI3wc4xRnPOrDjn4bZAOv2GsC3kSzyP4MeDtd4y9OtmFlH/nwWS");
        System.out.println(matches2);
    }

}
