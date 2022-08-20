package com.miaosha.result;

import lombok.*;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 16:28
 */

@Getter
public class Result<T> {
    private int code;
    private String msg;
    private T data;

    private Result(T data) {
        this.code = 0;
        this.msg = "success";
        this.data = data;
    }

    private Result(CodeMsg codeMsg){
        if (codeMsg==null) return;
        this.code = codeMsg.getCode();
        this.msg = codeMsg.getMsg();
    }

    public static <T> Result<T> success(T data){
        return new Result<>(data);
    }

    public static <T> Result<T> error(CodeMsg codeMsg){
        return new Result<>(codeMsg);
    }
}
