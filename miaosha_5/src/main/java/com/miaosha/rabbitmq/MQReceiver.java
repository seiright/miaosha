package com.miaosha.rabbitmq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

}
