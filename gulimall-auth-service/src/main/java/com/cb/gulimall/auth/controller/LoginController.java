package com.cb.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.cb.common.constant.AuthConstant;
import com.cb.common.exception.BzipCodeEnum;
import com.cb.common.utils.R;
import com.cb.common.vo.MemberLoginVo;
import com.cb.common.vo.MemberRegistVo;
import com.cb.common.constant.RedirectConstant;
import com.cb.gulimall.auth.feign.MemberFeignService;
import com.cb.gulimall.auth.feign.ThirdPartFeignService;
import com.cb.gulimall.auth.vo.LoginAcctVo;
import com.cb.common.vo.MemberInfoVo;
import com.cb.gulimall.auth.vo.RegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class LoginController {

    @Autowired
    private ThirdPartFeignService thirdPartFeignService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * 发送验证码
     * 1、接口防刷 todo
     * 2、手机号60s内存在验证码，不发送
     * 3、设置验证码缓存，并设置过期时间，缓存时带时间戳，方便判断
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (!StringUtils.isEmpty(redisCode)) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000) {
                return R.error(BzipCodeEnum.SMS_CODE_EXCEPTION.getCode(), BzipCodeEnum.SMS_CODE_EXCEPTION.getMsg());
            }
        }

        String code = UUID.randomUUID().toString().substring(0, 5);

        redisTemplate.opsForValue().set(
                AuthConstant.SMS_CODE_CACHE_PREFIX + phone,
                code + "_" + System.currentTimeMillis(),
                3, TimeUnit.MINUTES);

        thirdPartFeignService.sendCode(phone, code);

        return R.ok();
    }

    /**
     * TODO 重定向携带数据,利用session原理 将数据放在sessoin中 取一次之后删掉
     * <p>
     * TODO 1. 分布式下的session问题
     * 校验
     * RedirectAttributes redirectAttributes ： 模拟重定向带上数据
     * 注册成功，重定向到登录页
     * 注册失败，重定向到注册页面
     *
     * @param vo
     * @param result
     * @return
     */
    @PostMapping("/register")
    public String register(@Valid RegisterVo vo, BindingResult result, RedirectAttributes redirectAttributes) {
        // 数据校验失败，重定向到注册页面
        if (result.hasErrors()) {
            Map<String, String> errors = result.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
            redirectAttributes.addFlashAttribute("errors", errors);
            // post请求方式不支持,需要get方式
//            return "redirect:/reg.html";
            return RedirectConstant.REGIST_HTML_URL;
        }

        // 验证码校验
        String codeCacheKey = AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone();
        String codeStr = redisTemplate.opsForValue().get(codeCacheKey);
        if (StringUtils.isEmpty(codeStr)) {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return RedirectConstant.REGIST_HTML_URL;
        }
        String code = codeStr.split("_")[0];
        if (!code.equalsIgnoreCase(vo.getCode())) {
            Map<String, String> errors = new HashMap<>();
            errors.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", errors);
            return RedirectConstant.REGIST_HTML_URL;
        }
        redisTemplate.delete(codeCacheKey);

        // 用户模块注册
        MemberRegistVo registVo = new MemberRegistVo();
        BeanUtils.copyProperties(vo, registVo);
        R resistResult = memberFeignService.resist(registVo);
        if (resistResult.getCode() != 0) {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", resistResult.getOrDefault("msg", "").toString());
            redirectAttributes.addFlashAttribute("errors", errors);
            return RedirectConstant.REGIST_HTML_URL;
        }

        return RedirectConstant.LOGIN_HTML_URL;
    }

    @GetMapping("/login.html")
    public String login(HttpSession session){
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if(attribute == null){
            return "login";
        }
        return RedirectConstant.INDEX_HTML_URL;
    }


    @PostMapping("/login")
    public String login(LoginAcctVo vo, RedirectAttributes redirectAttributes, HttpSession session) {

        MemberLoginVo loginVo = new MemberLoginVo();
        BeanUtils.copyProperties(vo, loginVo);

        R login = memberFeignService.login(loginVo);
        if (login.getCode() != 0) {
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", login.getData("msg", new TypeReference<String>() {
            }));
            redirectAttributes.addFlashAttribute("errors", errors);
            return RedirectConstant.LOGIN_HTML_URL;
        }

        MemberInfoVo memberInfoVo = login.getData("data", new TypeReference<MemberInfoVo>() {
        });
        log.info("登录成功,用户：{}", memberInfoVo);

        // 第一次使用session 命令浏览器保存这个用户信息 JESSIONSEID 每次只要访问这个网站就会带上这个cookie
        // 在发卡的时候扩大session作用域 (指定域名为父域名 gulimall.com)
        // TODO 1.默认发的当前域的session (需要解决子域session共享问题)
        // TODO 2.使用JSON的方式序列化到redis
        // new Cookie("JSESSIONID","").setDomain("glmall.com");
        session.setAttribute(AuthConstant.LOGIN_USER, memberInfoVo);

        return RedirectConstant.INDEX_HTML_URL;
    }


}
