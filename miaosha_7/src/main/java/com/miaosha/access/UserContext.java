package com.miaosha.access;

import com.miaosha.domain.MiaoshaUser;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/19 19:07
 */

public class UserContext {
    private static final ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<>();

    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }

    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
