package com.miaosha.controller;

import com.miaosha.domain.MiaoshaUser;
import com.miaosha.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 16:47
 */

@Controller
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model, MiaoshaUser user) {
        return Result.success(user);
    }
}
