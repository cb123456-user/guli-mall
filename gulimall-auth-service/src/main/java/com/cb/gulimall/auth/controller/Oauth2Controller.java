package com.cb.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.cb.common.constant.AuthConstant;
import com.cb.common.constant.RedirectConstant;
import com.cb.common.utils.HttpUtils;
import com.cb.common.utils.R;
import com.cb.common.vo.SocialUser;
import com.cb.gulimall.auth.feign.MemberFeignService;
import com.cb.common.vo.MemberInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 社交登录
 */
@Slf4j
@Controller
@RequestMapping("/oauth2.0")
public class Oauth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * todo 测试
     * 由于微博开发者未认证成功，session共享先用账号登录测试
     *
     * 微博认证成功后跳转到当前请求
     * http://auth.gulimall.com/oauth2.0/weibo/success?code=xxxx
     * <p>
     * 交换token返回数据
     * {
     * "access_token": "ACCESS_TOKEN",
     * "expires_in": 1234,
     * "remind_in":"798114",
     * "uid":"12341234"
     * }
     */
    @GetMapping("/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {
        // 根据code 换取Access Token
        // https://api.weibo.com/oauth2/access_token?client_id=YOUR_CLIENT_ID&client_secret=YOUR_CLIENT_SECRET&grant_type=authorization_code&redirect_uri=YOUR_REGISTERED_REDIRECT_URI&code=CODE
        Map<String, String> map = new HashMap<>();
        map.put("client_id", "4254656811");
        map.put("client_secret", "7f3c8a4a9037bb7202a92ad87b75533f");
        map.put("grant_type", "authorization_code");
        map.put("code", code);
        map.put("redirect_uri", "http://auth.gulimall.com/oauth2.0/weibo/success");
        Map<String, String> headers = new HashMap<>();
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", headers, null, map);

        if (response.getStatusLine().getStatusCode() == 200) {
            String json = EntityUtils.toString(response.getEntity());
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            R loginResult = memberFeignService.oauth2Login(socialUser);
            if (loginResult.getCode() == 0) {

                MemberInfoVo memberInfoVo = loginResult.getData("data", new TypeReference<MemberInfoVo>() {
                });
                log.info("登录成功,用户：{}", memberInfoVo);

                // 第一次使用session 命令浏览器保存这个用户信息 JESSIONSEID 每次只要访问这个网站就会带上这个cookie
                // 在发卡的时候扩大session作用域 (指定域名为父域名 gulimall.com)
                // TODO 1.默认发的当前域的session (需要解决子域session共享问题)
                // TODO 2.使用JSON的方式序列化到redis
                // new Cookie("JSESSIONID","").setDomain("glmall.com");
                session.setAttribute(AuthConstant.LOGIN_USER, memberInfoVo);

                // 重定向到首页
                return RedirectConstant.INDEX_HTML_URL;
            }
        }
        // 错误重定向到登录页
        return RedirectConstant.LOGIN_HTML_URL;
    }
}
