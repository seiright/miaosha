package com.miaosha.controller;

import com.miaosha.access.AccessLimit;
import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.rabbitmq.MQSender;
import com.miaosha.rabbitmq.MiaoshaMessage;
import com.miaosha.redis.*;
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

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: 秒杀的控制方法
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

    private final Map<Long, Boolean> localOverMap = new HashMap<>();

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

    /**
     * 秒杀控制器初始化参数设置
     * @author zhaolifeng
     * @date 2022/8/20 18:32
     */
    @Override
    public void afterPropertiesSet(){
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), goods.getStockCount());
            localOverMap.put(goods.getId(), false);
        }
    }

    /**
     * 执行秒杀方法
     * @param model-模型
     * @param user-当前用户
     * @param goodsId-商品id
     * @param path-秒杀访问路径
     * @return 状态码
     * @author zhaolifeng
     * @date 2022/8/20 18:19
     */
    @PostMapping(value = "/{path}/do_miaosha", produces = "application/json")
    @ResponseBody
    public Result<Integer> miaosha(Model model, MiaoshaUser user,
                                   @RequestParam("goodsId") long goodsId,
                                   @PathVariable("path") String path) {
        model.addAttribute("user", user);
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证path
        boolean check = miaoshaService.checkPath(user,goodsId,path);
        if (!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //内存标记，减少redis访问
        Boolean over = localOverMap.get(goodsId);
        if (over) {
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);
        if (stock < 0) {
            localOverMap.put(goodsId, true);
            return Result.error(CodeMsg.MIAOSHA_OVER);
        }
        //判断是否重复秒杀
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if (order != null) {
            return Result.error(CodeMsg.MIAOSHA_REPEAT);
        }
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        sender.sendMiaoshaMessage(mm);
        return Result.success(0); //排队中
    }

    /**
     * 秒杀结果轮询
     * @param user-当前秒杀用户
     * @param goodsId-秒杀的商品id
     * @return 成功:返回订单id；失败:返回-1；排队中:返回0
     * @author zhaolifeng
     * @date 2022/8/18 15:21
     */
    @AccessLimit
    @GetMapping(value = "/result")
    @ResponseBody
    public Result<Long> miaoshaResult(MiaoshaUser user,
                                      @RequestParam("goodsId") long goodsId) {
        long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
        return Result.success(result);
    }


    /**
     * 如果验证码输出正确，则得到秒杀路径
     * @param user-当前用户
     * @param goodsId-商品id
     * @param verifyCode-用户输入的验证码
     * @return 秒杀路径
     * @author zhaolifeng
     * @date 2022/8/20 18:20
     */
    @AccessLimit(seconds = 5,maxCount=5)
    @GetMapping(value = "/path")
    @ResponseBody
    public Result<String> getMiaoshaPath(MiaoshaUser user,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode") Integer verifyCode) {
        boolean check = miaoshaService.checkVerifyCode(user,goodsId,verifyCode);
        if (!check){
            return Result.error(CodeMsg.VERIFY_CODE_INCORRECT);
        }
        String path = miaoshaService.createMiaoshaPath(user,goodsId);
        return Result.success(path);
    }

    /**
     * 根据不同用户id和商品id生成特定的验证码
     * @param response-客户端响应
     * @param user-当前用户
     * @param goodsId-商品id
     * @return 错误则返回状态码
     * @author zhaolifeng
     * @date 2022/8/20 18:28
     */
    @AccessLimit
    @GetMapping(value = "/verifyCode")
    @ResponseBody
    public Result<String> getMiaoshaVerifyCode(HttpServletResponse response, MiaoshaUser user,
                                               @RequestParam("goodsId") long goodsId) {
        BufferedImage image = miaoshaService.createVerifyCode(user,goodsId);
        try {
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

    /**
     * 测试用方法-重置数据库和redis
     * @return 重置结果
     * @author zhaolifeng
     * @date 2022/8/20 18:25
     */
    @RequestMapping(value = "/reset", method = RequestMethod.GET)
    @ResponseBody
    public Result<Boolean> reset() {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        for (GoodsVo goods : goodsList) {
            goods.setStockCount(10);
            redisService.set(GoodsKey.getMiaoshaGoodsStock, "" + goods.getId(), 10);
            localOverMap.put(goods.getId(), false);
        }
        redisService.delete(OrderKey.getMiaoshaOrderByUidGid);
        redisService.delete(MiaoshaKey.isGoodsOver);
        miaoshaService.reset(goodsList);
        return Result.success(true);
    }

}
