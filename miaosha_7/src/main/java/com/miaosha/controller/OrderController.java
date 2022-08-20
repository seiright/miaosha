package com.miaosha.controller;

import com.miaosha.access.AccessLimit;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.domain.OrderInfo;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;
import com.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/17 15:58
 */

@Controller
@RequestMapping("/order")
public class OrderController {

    OrderService orderService;
    GoodsService goodsService;

    @Autowired
    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    @Autowired
    public void setGoodsService(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * 订单详情查询方法
     * @param orderId-订单id
     * @return 订单详情对象
     * @author zhaolifeng
     * @date 2022/8/20 18:33
     */
    @AccessLimit
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(@RequestParam("orderId") long orderId){
        OrderInfo order = orderService.getOrderById(orderId);
        if (order==null){
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        Long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
