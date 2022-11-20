package com.cb.gulimall.cart.interceptor;

import com.cb.common.constant.AuthConstant;
import com.cb.common.constant.CartConstant;
import com.cb.common.to.UserInfoTo;
import com.cb.common.vo.MemberInfoVo;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * @authoer: chenbin
 * @date: 2022/8/28/028 17:03
 * @description:
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

    public static ThreadLocal<UserInfoTo> userInfoToThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        // 获取session
        HttpSession httpSession = request.getSession();
        MemberInfoVo memberInfo = (MemberInfoVo) httpSession.getAttribute(AuthConstant.LOGIN_USER);

        /**
         * 用户已登录
         */
        if (memberInfo != null) {
            userInfoTo.setUserId(memberInfo.getId());
        }

        /**
         * 临时用户
         */
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (CartConstant.TEMP_USER_COOKIE_NAME.equalsIgnoreCase(cookie.getName())) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                    break;
                }
            }
        }

        /**
         * 创建临时用户
         */
        if (userInfoTo.getUserKey() == null) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }


        userInfoToThreadLocal.set(userInfoTo);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        UserInfoTo userInfoTo = userInfoToThreadLocal.get();

        // 命令浏览器保存临时用户信息
        if (!userInfoTo.getTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            // 过期时间
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            // 作用域
            cookie.setDomain(AuthConstant.COOKIE_DOMAIN_NAME);
            response.addCookie(cookie);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 调用删除方法，是必须选项。因为使用的是tomcat线程池，请求结束后，线程不会结束。
        // 如果不手动删除线程变量，可能会导致内存泄漏
        userInfoToThreadLocal.remove();
    }
}
