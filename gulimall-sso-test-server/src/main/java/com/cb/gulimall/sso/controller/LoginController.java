package com.cb.gulimall.sso.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @GetMapping("/user")
    @ResponseBody
    public String employees(@RequestParam("token") String token) {
        return redisTemplate.opsForValue().get(token);
    }

    @GetMapping("/login.html")
    public String login(
            Model model,
            @RequestParam("redirect_url") String redirectUrl,
            @CookieValue(value = "sso_token", required = false) String ssoToken
    ) {

        if (!StringUtils.isEmpty(ssoToken)) {
            return "redirect:" + redirectUrl + "?token=" + ssoToken;
        }

        model.addAttribute("url", redirectUrl);
        return "login";
    }

    @PostMapping("/doLogin")
    public String doLogin(
            @RequestParam("url") String url,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            HttpServletResponse response
    ) {

        // 生成token
        String token = UUID.randomUUID().toString().replaceAll("-", "");

        // 登录成功，保存用户信息
        redisTemplate.opsForValue().set(token, username);

        // 命令浏览器保存浏览痕迹
        Cookie cookie = new Cookie("sso_token", token);
        response.addCookie(cookie);

        return "redirect:" + url + "?token=" + token;
    }

}
