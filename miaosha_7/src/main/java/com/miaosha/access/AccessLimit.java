package com.miaosha.access;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 可以单独设置登录限制。也可以同时设置访问限制和用户登录限制。
 * @description: 访问控制
 * @author zhaolifeng
 * @date 2022/8/19 23:31
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimit {
    int seconds() default -1;
    int maxCount() default -1;
    boolean needLogin() default true;
}
