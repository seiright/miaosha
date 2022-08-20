package com.miaosha.rabbitmq;

import com.miaosha.redis.RedisService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.amqp.rabbit.core.RabbitTemplate;


/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/18 13:09
 */


@Service
public class MQSender {

    RabbitTemplate rabbitTemplate;

    @Autowired
    public void setRabbitTemplate(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMiaoshaMessage(MiaoshaMessage mm) {
        String msg = RedisService.beanToString(mm);
        rabbitTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }

    /*
    public void send(Object message){
        String msg = RedisService.beanToString(message);
        rabbitTemplate.convertAndSend(MQConfig.QUEUE,msg);
        log.info("send message:" + msg);
    }

    public void sendTopic(Object message){
        String msg = RedisService.beanToString(message);
        rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY1,msg+"1");
        rabbitTemplate.convertAndSend(MQConfig.TOPIC_EXCHANGE,MQConfig.ROUTING_KEY2,msg+"2");
        log.info("send message:" + msg);
    }

    public void sendFanout(Object message){
        String msg = RedisService.beanToString(message);
        rabbitTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"",msg);
        log.info("send message:" + msg);
    }

    public void sendHeader(Object message){
        String msg = RedisService.beanToString(message);
        MessageProperties properties = new MessageProperties();
        properties.setHeader("header1","value1");
        properties.setHeader("header2","value2");
        Message obj = new Message(msg.getBytes(StandardCharsets.UTF_8),properties);
        rabbitTemplate.convertAndSend(MQConfig.HEADER_EXCHANGE,"",obj);
        log.info("send message:" + msg);
    }
     */
}
