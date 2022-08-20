package com.miaosha.vo;

import com.miaosha.domain.Goods;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 12:56
 */

@Data
public class GoodsVo extends Goods {
    private BigDecimal miaoshaPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
