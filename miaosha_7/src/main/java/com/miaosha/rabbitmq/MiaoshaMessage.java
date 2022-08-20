package com.miaosha.rabbitmq;

import com.miaosha.domain.MiaoshaUser;
import lombok.Data;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/18 14:56
 */

@Data
public class MiaoshaMessage {
    private MiaoshaUser user;
    private long goodsId;
}
