package com.cb.gulimall.member.exceptiion;

public class UserNameExistException extends RuntimeException{
    public UserNameExistException() {
        super("用户名已存在");
    }
}
