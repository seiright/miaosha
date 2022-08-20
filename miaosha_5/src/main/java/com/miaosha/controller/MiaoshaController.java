package com.miaosha.controller;

import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.domain.OrderInfo;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 14:41
 */

@RequestMapping("/miaosha")
@Controller
public class MiaoshaController {
    MiaoshaUserService userService;
    RedisService redisService;
    GoodsService goodsService;
    OrderService orderService;
    MiaoshaService miaoshaService;

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setGoodsService(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @Autowired
    public void setUserService(MiaoshaUserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setMiaoshaService(MiaoshaService miaoshaService) {
        this.miaoshaService = miaoshaService;
    }

    @PostMapping(value = "/do_miaosha",produces = "application/json")
    @ResponseBody
    public Result<OrderInfo> list(Model model, MiaoshaUser user,
                                  @RequestParam("goodsId") long goodsId){
        model.addAttribute("user",user);
        if (user==null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //判断库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stockCount = goods.getStockCount();
        if (stockCount<=0){
            model.addAttribute("errmsg", CodeMsg.MIAOSHA_OVER);
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //判断是否重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){
            model.addAttribute("errmsg",CodeMsg.MIAOSHA_REPEAT);
            return Result.error(CodeMsg.MIAOSHA_REPEAT);
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);
        return Result.success(orderInfo);

    }

}
