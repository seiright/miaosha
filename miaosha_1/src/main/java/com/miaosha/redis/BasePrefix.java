package com.miaosha.redis;

import lombok.AllArgsConstructor;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 21:01
 */

@AllArgsConstructor
public abstract class BasePrefix implements KeyPrefix{

    private int expireSeconds;
    private String prefix;

    public BasePrefix(String prefix) {
        this(0,prefix);
    }

    @Override
    public int expireSeconds() { //默认0代表永不过期
        return expireSeconds;
    }

    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();
        return className + ":" + prefix;
    }
}
