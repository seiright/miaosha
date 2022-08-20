package com.miaosha.service;

import com.miaosha.exception.GlobalException;
import com.miaosha.dao.MiaoshaUserDao;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.MiaoshaUserKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.util.MD5Util;
import com.miaosha.util.UUIDUtil;
import com.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/13 18:09
 */

@Service
public class MiaoshaUserService {
    MiaoshaUserDao miaoshaUserDao;
    RedisService redisService;

    public static final String COOKIE_NAME_TOKEN = "token";

    @Autowired
    public MiaoshaUserService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setMiaoshaUserDao(MiaoshaUserDao miaoshaUserDao) {
        this.miaoshaUserDao = miaoshaUserDao;
    }

    public MiaoshaUser getById(long id){
        return miaoshaUserDao.getById(id);
    }

    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        if (loginVo==null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if (user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass,saltDB);
        if (!calcPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        String token = UUIDUtil.uuid();
        //生成cookie
        addCookie(response,token,user);
        return true;
    }

    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
        if (user!=null){
            //延长有效期
            addCookie(response,token,user);
        }
        return user;
    }

    private void addCookie(HttpServletResponse response,String token,MiaoshaUser user){
        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
