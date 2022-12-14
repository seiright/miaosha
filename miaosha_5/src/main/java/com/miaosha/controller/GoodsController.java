package com.miaosha.controller;

import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.vo.GoodsDetailVo;
import com.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/13 19:34
 */

@Controller
@RequestMapping("/goods")
public class GoodsController {
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    MiaoshaUserService userService;
    RedisService redisService;
    GoodsService goodsService;
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    public void setThymeleafViewResolver(ThymeleafViewResolver thymeleafViewResolver) {
        this.thymeleafViewResolver = thymeleafViewResolver;
    }

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


    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,Model model, MiaoshaUser user){
        model.addAttribute("user",user);
        //?????????
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
        //????????????
        WebContext webContext = new WebContext(request,response,request.getServletContext(),response.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);
        //???????????????
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    @RequestMapping(value = "/detail/{goodsId}",produces = "application/json")
    @ResponseBody
    public Result<GoodsDetailVo> detail(MiaoshaUser user, @PathVariable("goodsId") long goodsId){
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus;
        int remainSeconds;
        if (now<startAt){ //??????????????????
            remainSeconds = (int) ((startAt-now)/1000);
            miaoshaStatus = 0;
        }else if (now>endAt){ //??????????????????
            remainSeconds = -1;
            miaoshaStatus = 2;
        }else { //???????????????
            remainSeconds = 0;
            miaoshaStatus = 1;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setUser(user);
        vo.setGoods(goods);
        vo.setMiaoshaStatus(miaoshaStatus);
        vo.setRemainSeconds(remainSeconds);
        return Result.success(vo);
    }

    //??????????????????
    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String renderDetail(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user,
                         @PathVariable("goodsId") long goodsId){
        //??????id????????????????????????????????????
        model.addAttribute("user",user);
        //?????????
        String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        model.addAttribute("goods",goods);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus;
        int remainSeconds;
        if (now<startAt){ //??????????????????
            remainSeconds = (int) ((startAt-now)/1000);
            miaoshaStatus = 0;
        }else if (now>endAt){ //??????????????????
            remainSeconds = -1;
            miaoshaStatus = 2;
        }else { //???????????????
            remainSeconds = 0;
            miaoshaStatus = 1;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        WebContext webContext = new WebContext(request,response,request.getServletContext(),response.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", webContext);
        //???????????????
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

}
