package com.cb.gulimall.order.interceptor;

import com.cb.common.constant.AuthConstant;
import com.cb.common.constant.RedirectConstant;
import com.cb.common.vo.MemberInfoVo;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @authoer: chenbin
 * @date: 2022/11/12/012 23:04
 * @description:
 */
@Component
public class OrderLoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberInfoVo> threadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        MemberInfoVo memberInfoVo = (MemberInfoVo) request.getSession().getAttribute(AuthConstant.LOGIN_USER);

        if (memberInfoVo != null) {
            threadLocal.set(memberInfoVo);
            return true;
        } else {
            response.sendRedirect(RedirectConstant.LOGIN_HTML);
            return false;
        }
    }
}
