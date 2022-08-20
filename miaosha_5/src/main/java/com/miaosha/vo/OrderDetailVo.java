package com.miaosha.vo;

import com.miaosha.domain.OrderInfo;
import lombok.Data;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/17 15:59
 */

@Data
public class OrderDetailVo {
    private GoodsVo goods;
    private OrderInfo order;
}
