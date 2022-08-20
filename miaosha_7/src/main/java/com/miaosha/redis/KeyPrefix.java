package com.miaosha.redis;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 20:59
 */
public interface KeyPrefix {
    int expireSeconds();
    String getPrefix();
}
