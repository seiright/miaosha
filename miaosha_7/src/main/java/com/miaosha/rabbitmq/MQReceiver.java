package com.miaosha.rabbitmq;

import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.RedisService;
import com.miaosha.service.GoodsService;
import com.miaosha.service.MiaoshaService;
import com.miaosha.service.OrderService;
import com.miaosha.vo.GoodsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/18 13:09
 */

@Slf4j
@Service
public class MQReceiver {
    GoodsService goodsService;
    OrderService orderService;
    MiaoshaService miaoshaService;

    @Autowired
    public MQReceiver(GoodsService goodsService, OrderService orderService, MiaoshaService miaoshaService) {
        this.goodsService = goodsService;
        this.orderService = orderService;
        this.miaoshaService = miaoshaService;
    }

    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void receive(String message){
        MiaoshaMessage mm = RedisService.stringToBean(message, MiaoshaMessage.class);
        MiaoshaUser user = mm.getUser();
        long goodsId = mm.getGoodsId();
        //判断是否还有库存
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Integer stockCount = goods.getStockCount();
        if (stockCount<=0){
            return ;
        }
        //判断是否已经秒杀到
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(),goodsId);
        if (order!=null){
            return ;
        }
        miaoshaService.miaosha(user,goods);
    }

    /*
    @RabbitListener(queues = MQConfig.QUEUE)
    public void receive(String message){
        log.info("receive message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message){
        log.info("topic queue1 receive message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message){
        log.info("topic queue2 receive message:" + message);
    }

    @RabbitListener(queues = MQConfig.HEADER_QUEUE)
    public void receiveHeader(Message message){
        String msg = new String(message.getBody());
        log.info("header queue receive message:" + msg);
    }
     */



}
