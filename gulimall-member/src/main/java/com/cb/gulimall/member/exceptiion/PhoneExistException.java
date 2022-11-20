package com.cb.gulimall.member.exceptiion;

public class PhoneExistException extends RuntimeException{
    public PhoneExistException() {
        super("电话号码已存在");
    }
}
