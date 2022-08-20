package com.miaosha.exception;

import com.miaosha.result.CodeMsg;
import lombok.Getter;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/13 18:49
 */

@Getter
public class GlobalException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg){
        super(codeMsg.toString());
        this.codeMsg = codeMsg;
    }
}
