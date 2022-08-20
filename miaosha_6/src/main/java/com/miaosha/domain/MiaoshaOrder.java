package com.miaosha.domain;

import lombok.Data;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 12:52
 */
@Data
public class MiaoshaOrder {
    private Long id;
    private Long userId;
    private Long  orderId;
    private Long goodsId;
}
