package com.miaosha.controller;

import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.GoodsKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.Result;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.vo.GoodsDetailVo;
import com.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: 商品相关控制器
 * @date 2022/8/13 19:34
 */

@Controller
@RequestMapping("/goods")
public class GoodsController {
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

    /**
     * 因为商品列表页面刷新频率不高，故手动thymeleaf渲染商品页面。然后将页面存入redis
     * 访问时，会优先从redis里面取页面。若没取到，则重新渲染存入redis。
     * 商品页面的TTL为60s。
     * @param request-客户端请求
     * @param response-客户端响应
     * @param model-模型
     * @return 经过渲染后的商品列表的页面
     * @author zhaolifeng
     * @date 2022/8/20 15:47
     */
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request, HttpServletResponse response,Model model){
        //取缓存
        String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
        if (!StringUtils.isEmpty(html)){
            return html;
        }
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        model.addAttribute("goodsList",goodsList);
        //手动渲染
        WebContext webContext = new WebContext(request,response,request.getServletContext(),response.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", webContext);
        //做页面缓存
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    /**
     * 获取商品详情，并以json格式返回给前端。
     * @param user-访问用户
     * @param goodsId-要查看详情的商品id
     * @return 商品详情对象
     * @author zhaolifeng
     * @date 2022/8/20 15:52
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(MiaoshaUser user, @PathVariable("goodsId") long goodsId){
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus;
        int remainSeconds;
        if (now<startAt){ //秒杀还没开始
            remainSeconds = (int) ((startAt-now)/1000);
            miaoshaStatus = 0;
        }else if (now>endAt){ //秒杀已经结束
            remainSeconds = -1;
            miaoshaStatus = 2;
        }else { //秒杀进行中
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

    /**
     * 因为商品详情刷新频率较高，因此实际上没有用到这个控制器
     * @param request-客户端请求
     * @param response-客户端相应
     * @param model-模型
     * @param user-访问用户
     * @param goodsId-商品id
     * @return 商品详情页面
     * @author zhaolifeng
     * @date 2022/8/20 15:54
     */
    @RequestMapping(value = "/to_detail/{goodsId}",produces = "text/html")
    @ResponseBody
    public String detail2(HttpServletRequest request,HttpServletResponse response,Model model, MiaoshaUser user,
                         @PathVariable("goodsId") long goodsId){
        //商品id一般不用自增，用雪花算法
        model.addAttribute("user",user);
        //取缓存
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
        if (now<startAt){ //秒杀还没开始
            remainSeconds = (int) ((startAt-now)/1000);
            miaoshaStatus = 0;
        }else if (now>endAt){ //秒杀已经结束
            remainSeconds = -1;
            miaoshaStatus = 2;
        }else { //秒杀进行中
            remainSeconds = 0;
            miaoshaStatus = 1;
        }
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        model.addAttribute("remainSeconds",remainSeconds);

        WebContext webContext = new WebContext(request,response,request.getServletContext(),response.getLocale(),model.asMap());
        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", webContext);
        //做页面缓存
        if (!StringUtils.isEmpty(html)){
            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
        }
        return html;
    }

}
