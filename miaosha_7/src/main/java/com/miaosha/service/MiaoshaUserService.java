package com.miaosha.service;

import com.miaosha.dao.MiaoshaUserDao;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.exception.GlobalException;
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
 * @description: 秒杀服务方法
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

    /**
     * 根据用户id得到用户完整信息
     * @param id-用户id
     * @return 查询到的用户
     * @author zhaolifeng
     * @date 2022/8/20 16:09
     */
    public MiaoshaUser getById(long id){
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, "" + id, MiaoshaUser.class);
        if (user!=null){
            return user;
        }
        //取数据库
        user = miaoshaUserDao.getById(id);
        if (user!=null){
            redisService.set(MiaoshaUserKey.getById, "" + id, user);
        }
        return user;
    }


    /**
     * 更新密码
     * @param token-用户的token
     * @param id-用户的id
     * @param formPass-md5salt值
     * @return boolean 
     * @author zhaolifeng
     * @date 2022/8/20 16:13
     */
    @SuppressWarnings("unused")
    public boolean updatePassword(String token,long id, String formPass){
        //取user
        MiaoshaUser user = getById(id);
        if (user==null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        MiaoshaUser toBeUpdate = new MiaoshaUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        miaoshaUserDao.update(toBeUpdate);
        //处理缓存
        redisService.delete(MiaoshaUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(MiaoshaUserKey.token,token,user);
        return true;
    }

    /**
     * 登录方法
     * @param response-客户端响应
     * @param loginVo-登录信息
     * @return java.lang.String
     * @author zhaolifeng
     * @date 2022/8/20 16:13
     */
    public String login(HttpServletResponse response, LoginVo loginVo) {
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
        return token;
    }

    /**
     * 根据token值获取用户对象。
     * 获取的同时会延长token有效期
     * @param response-客户端响应
     * @param token-token值
     * @return com.miaosha.domain.MiaoshaUser
     * @author zhaolifeng
     * @date 2022/8/20 16:29
     */
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

    /**
     * 将随机生成的token值写入客户端的cookie中。
     * 同时向redis里面写入键值为token的用户对象缓存。
     * @param response-客户端响应
     * @param token-token(是一个UUID)
     * @param user-当前登录用户
     * @author zhaolifeng
     * @date 2022/8/20 16:22
     */
    private void addCookie(HttpServletResponse response,String token,MiaoshaUser user){
        redisService.set(MiaoshaUserKey.token,token,user);
        Cookie cookie = new Cookie(COOKIE_NAME_TOKEN,token);
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
