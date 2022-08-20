package com.miaosha.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 12:50
 */

@Data
public class MiaoshaGoods {
    private Long id;
    private Long goodsId;
    private BigDecimal miaoshaPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
