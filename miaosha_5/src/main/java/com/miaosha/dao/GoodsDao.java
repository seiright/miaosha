package com.miaosha.dao;

import com.miaosha.domain.MiaoshaGoods;
import com.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 12:56
 */

@Mapper
public interface GoodsDao {


    @Select("select g.*, mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg left outer join goods g on g.id = mg.goods_id")
    List<GoodsVo> listGoodsVo();

    @Select("select g.*, mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg left outer join goods g on g.id = mg.goods_id where g.id=#{goodsId}")
    GoodsVo getGoodsVoByGoodsId(@Param("goodsId") long goodsId);

    @Update("update miaosha_goods set stock_count = stock_count-1 where goods_id = #{goodsId}")
    int reduceStock(MiaoshaGoods g);
}
