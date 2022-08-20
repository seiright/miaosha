package com.miaosha.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 16:37
 */

@AllArgsConstructor
@Getter
public class CodeMsg {
    private int code;
    private String meg;

    //通用异常
    public static CodeMsg SUCCESS = new  CodeMsg(0,"success");
    public static CodeMsg SERVER_ERROR = new  CodeMsg(500100,"服务端异常");

    //登录模块 5002XX

    //商品模块 5003XX

    //订单模块 5004XX

    //秒杀模块 5005XX
}
