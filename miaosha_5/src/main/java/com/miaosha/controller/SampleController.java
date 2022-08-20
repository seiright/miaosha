package com.miaosha.controller;

import com.miaosha.domain.User;
import com.miaosha.rabbitmq.MQReceiver;
import com.miaosha.rabbitmq.MQSender;
import com.miaosha.redis.RedisService;
import com.miaosha.redis.UserKey;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/12 16:05
 */

@RequestMapping("/demo")
@Controller
public class SampleController {

    UserService userService;
    RedisService redisService;
    MQSender sender;
    MQReceiver receiver;

    @Autowired
    public void setSender(MQSender sender) {
        this.sender = sender;
    }

    @Autowired
    public void setReceiver(MQReceiver receiver) {
        this.receiver = receiver;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }


    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name","zlf");
        return "hello";
    }

    @RequestMapping("/hello")
    @ResponseBody
    public Result<String> hello(){
        return Result.success("hello,nihao");
    }

    @RequestMapping("/helloError")
    @ResponseBody
    public Result<String> helloError(){
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx(){
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById,""+1, User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User(1, "1111");

        Boolean v1 = redisService.set(UserKey.getById,""+1, user);

        return Result.success(true);
    }

    @RequestMapping("/mq/direct")
    @ResponseBody
    public Result<String> mq(){
        sender.send("hello,lf");
        return Result.success("hello,world");
    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqTopic(){
        sender.sendTopic("hello,lf");
        return Result.success("hello,world");
    }

    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqFanout(){
        sender.sendFanout("hello,lf");
        return Result.success("hello,world");
    }

    @RequestMapping("/mq/header")
    @ResponseBody
    public Result<String> mqHeader(){
        sender.sendHeader("hello,lf");
        return Result.success("hello,world");
    }

}
