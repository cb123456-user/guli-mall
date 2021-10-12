package com.cb.gulimall.product.exception;

import com.cb.common.exception.BzipCodeEnum;
import com.cb.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品服务全局异常处理器
 */
@Slf4j
@RestControllerAdvice(basePackages = {"com.cb.gulimall.product.controller"})
public class ExceptionHandleController {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R validException(MethodArgumentNotValidException e) {
        log.error("param valid faild, class {}, error {}", e.getClass(), e.getMessage());
        Map<String, String> map = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(it -> map.put(it.getField(), it.getDefaultMessage()));
        return R.error(BzipCodeEnum.VALID_FAILD_EXCEPTION.getCode(), BzipCodeEnum.VALID_FAILD_EXCEPTION.getMsg())
                .put("data", map);
    }

    @ExceptionHandler(Throwable.class)
    public R Throwable(Throwable e) {
        log.error("param valid faild, class {}, error {}", e.getClass(), e.getMessage());
        return R.error(BzipCodeEnum.UNKNOW_EXCEPTION.getCode(), BzipCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
