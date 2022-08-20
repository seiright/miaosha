package com.miaosha.controller;

import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @description: TODO
 * @date 2022/8/13 17:01
 */


@RequestMapping(value = {"/","/login"})
@Controller
public class LoginController {

    private static Logger log = LoggerFactory.getLogger(LoginController.class);

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

    @RequestMapping(value = {"/","/to_login"})
    public String toLogin(){
        return "login";
    }

    @PostMapping ("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        //登录
        String token = userService.login(response,loginVo);
        return Result.success(token);
    }
}
