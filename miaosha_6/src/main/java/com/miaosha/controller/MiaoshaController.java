package com.miaosha.controller;

import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.rabbitmq.MQSender;
import com.miaosha.rabbitmq.MiaoshaMessage;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.MiaoshaKey;
import com.miaosha.redis.OrderKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 14:41
 */

@RequestMapping("/miaosha")
@Controller
public class MiaoshaController implements InitializingBean {

    MiaoshaUserService userService;
    RedisService redisService;
    GoodsService goodsService;
    OrderService orderService;
    MiaoshaService miaoshaService;
    MQSender sender;

    private Map<Long,Boolean> localOverMap = new HashMap<>();

    @Autowired
    public MiaoshaController(MiaoshaUserService userService, RedisService redisService,
                             GoodsService goodsService, OrderService orderService,
                             MiaoshaService miaoshaService, MQSender sender) {
        this.userService = userService;
        this.redisService = redisService;
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.miaoshaService = miaoshaService;
        this.sender = sender;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList==null){
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock,""+goods.getId(),goods.getStockCount());
            localOverMap.put(goods.getId(),false);
        }
    }

    //??????????????????redis
    @RequestMapping(value="/reset", method=RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset(Model model) {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for(GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

    @PostMapping(value = "/do_miaosha",produces = "application/json")
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                       @RequestParam("goodsId") long goodsId){
        model.addAttribute("user",user);
        if (user==null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //?????????????????????redis??????
        Boolean over = localOverMap.get(goodsId);
        if (over){
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //????????????
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock,""+goodsId);
        if (stock<0){
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //????????????????????????
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){
            model.addAttribute("errmsg",CodeMsg.MIAOSHA_REPEAT);
            return Result.error(CodeMsg.MIAOSHA_REPEAT);
        }
        //??????
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0); //?????????

        /*
        //????????????
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stockCount = goods.getStockCount();
        if (stockCount<=0){
            model.addAttribute("errmsg", CodeMsg.MIAOSHA_OVER);
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //????????????????????????
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){
            model.addAttribute("errmsg",CodeMsg.MIAOSHA_REPEAT);
            return Result.error(CodeMsg.MIAOSHA_REPEAT);
        }
        //????????? ????????? ??????????????????
        OrderInfo orderInfo = miaoshaService.miaosha(user,goods);
        model.addAttribute("orderInfo",orderInfo);
        model.addAttribute("goods",goods);
        return Result.success(orderInfo);

         */

    }

    /**
     * ??????????????????
     * @param model-model
     * @param user-??????????????????
     * @param goodsId-???????????????id
     * @return ??????:????????????id?????????:??????-1????????????:??????0
     * @author zhaolifeng
     * @date 2022/8/18 15:21
     */
    @GetMapping(value = "/result",produces = "application/json")
    @ResponseBody
    public Result<Long> miaoshaResult(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long result = miaoshaService.getMiaoshaResult(user.getId(),goodsId);
        return Result.success(result);
    }

}
