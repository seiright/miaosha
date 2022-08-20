package com.miaosha.controller;

import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: 登录控制器
 * @date 2022/8/13 17:01
 */
@RequestMapping(value = {"/","/login"})
@Controller
public class LoginController {

    MiaoshaUserService userService;
    RedisService redisService;

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setUserService(MiaoshaUserService userService) {
        this.userService = userService;
    }

    /**
     * 跳转登录页面
     * @return 登录页面
     * @author zhaolifeng
     * @date 2022/8/20 15:58
     */
    @RequestMapping(value = {"/","/to_login"})
    public String toLogin(){
        return "login";
    }

    /**
     * 客户端登录的请求控制器
     * @param response-客户端相应
     * @param loginVo-登录信息
     * @return 处理登录信息
     * @author zhaolifeng
     * @date 2022/8/20 15:59
     */
    @PostMapping ("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        //登录
        String token = userService.login(response,loginVo);
        return Result.success(token);
    }
}
