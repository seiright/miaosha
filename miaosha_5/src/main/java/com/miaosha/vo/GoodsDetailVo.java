package com.miaosha.vo;

import com.miaosha.domain.MiaoshaUser;
import lombok.Data;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/17 13:07
 */

@Data
public class GoodsDetailVo {
    private int miaoshaStatus;
    private int remainSeconds;
    private GoodsVo goods;
    private MiaoshaUser user;
}
