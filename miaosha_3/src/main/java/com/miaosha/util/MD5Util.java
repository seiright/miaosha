package com.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: MD5加密工具类
 * @date 2022/8/13 16:50
 */


public class MD5Util {
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    private static final String salt = "1a2b3c4d"; //防止彩虹表攻击

    //服务端md5加密
    public static String inputPassFormPass(String inputPass){
        //做此处理，若salt设置为"1a2b3c4d".攻击者通过查询彩虹表攻击密码“123456”得到的结果为“12123456c3”。
        String str = "" + salt.charAt(0)+salt.charAt(2) + inputPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    //客户端md5加密
    public static String formPassToDBPass(String formPass,String salt){
        //做此处理，攻击者通过查询彩虹表攻击密码“123456”得到的结果为“12123456c3”。
        String str = "" + salt.charAt(0)+salt.charAt(2) + formPass + salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    public static String inputPassToDBPass(String input,String saltDB){
        String formPass = inputPassFormPass(input);
        String dbPass = formPassToDBPass(formPass,saltDB);
        return dbPass;
    }

}
