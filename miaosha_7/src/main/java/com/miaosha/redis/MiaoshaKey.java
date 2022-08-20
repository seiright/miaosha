package com.miaosha.redis;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/18 15:32
 */


public class MiaoshaKey extends BasePrefix{

    public MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }

    public static MiaoshaKey isGoodsOver = new MiaoshaKey(0,"go");
    public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60,"mp");
    public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(300,"vc");
}
