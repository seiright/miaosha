package com.miaosha.redis;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/13 19:26
 */
public class MiaoshaUserKey extends BasePrefix{

    public static final int TOKEN_EXPIRE = 3600*24*2;

    public MiaoshaUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public MiaoshaUserKey(String prefix) {
        super(prefix);
    }

    public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE,"tk");
}
