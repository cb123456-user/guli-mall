package com.cb.gulimall.ssotest.controller;

import com.cb.gulimall.ssotest.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class BossController {

    @Value("${sso.url}")
    private String ssoUrl;

    @Value("${local.url}")
    private String localUrl;

    /**
     * 资源受保护，需要登录
     * 登录判断：
     * 1、token
     * 2、session有登录信息
     *
     * @param model
     * @param session
     * @return
     */
    @GetMapping("/boss")
    public String employees(
            Model model,
            HttpSession session,
            @RequestParam(value = "token", required = false) String token
    ) throws Exception {

        if (!StringUtils.isEmpty(token)) {
            // todo 从认证中心获取用户信息，保存
//            session.setAttribute("user", "dada");
            Map<String, String> headers = new HashMap<>();
            Map<String, String> querys = new HashMap<>();
            querys.put("token", token);
            HttpResponse response = HttpUtils.doGet("http://ssoserver.com:8080", "/user", "get", headers, querys);
            if (response.getStatusLine().getStatusCode() == 200) {
                String user = EntityUtils.toString(response.getEntity());
                session.setAttribute("user", user);
            }
        }

        Object loginUser = session.getAttribute("user");
        if (loginUser == null) {
            return "redirect:" + ssoUrl + "?redirect_url=" + localUrl;
        } else {
            List<String> emps = new ArrayList<>();
            emps.add("张三");
            emps.add("李四");
            model.addAttribute("emps", emps);
            return "list";
        }
    }
}
