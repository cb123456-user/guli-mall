package com.cb.gulimall.thirdpart.controller;

import com.cb.common.utils.R;
import com.cb.gulimall.thirdpart.component.SmsComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsComponent smsComponent;

    /**
     * 发送验证码，不是前段直接调用，提供给其他服务调用的
     * @param phone
     * @param code
     * @return
     */
    @GetMapping("/send/code")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsComponent.sendCode(phone, code);
        return R.ok();
    }
}
