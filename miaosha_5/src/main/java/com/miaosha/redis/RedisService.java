package com.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;


/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 19:16
 */

@Service
public class RedisService {

    JedisPool jedisPool;

    @Autowired
    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    //获取单个对象
    public <T> T get(KeyPrefix prefix, String key, Class<T> clazz){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }


    //设置对象
    public <T> Boolean set(KeyPrefix prefix,String key, T value){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (!StringUtils.hasLength(str)){
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();
            if (seconds<=0){
                jedis.set(realKey,str);
            }else {
                jedis.setex(realKey,seconds,str); //设置键值的同时设置过期时间。
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    //判断key是否存在
    public <T> Boolean exists(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //将 key 中储存的数字值增1。只能对数字值操作
    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //将 key 中储存的数字值减1。只能对数字值操作
    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    //删除操作
    public boolean delete(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            return ret>0;
        }finally {
            returnToPool(jedis);
        }
    }

    //bean对象转string
    public static  <T> String beanToString(T value) {
        if (value==null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == Integer.class){
            return "" + value;
        }else if (clazz == String.class){
            return (String) value;
        }else if (clazz == Long.class){
            return "" + value;
        }else {
            return JSON.toJSONString(value);  //认为是bean类
        }
    }

    //string转bean对象
    @SuppressWarnings("unchecked")
    public static  <T> T stringToBean(String str,Class<T> clazz) {
        if (!StringUtils.hasLength(str)||clazz==null){
            return null;
        }
        if (clazz==int.class || clazz == Integer.class){
            return (T) Integer.valueOf(str);
        }else if (clazz == String.class){
            return (T) str;
        }else if (clazz==long.class||clazz==Long.class){
            return (T) Long.valueOf(str);
        }else {
            return JSON.toJavaObject(JSON.parseObject(str),clazz);  //认为是bean类
        }
    }

    private void returnToPool(Jedis jedis) {
        if (jedis!=null){
            jedis.close();
        }
    }
}
