package com.miaosha.dao;

import com.miaosha.domain.MiaoshaOrder;
import com.miaosha.domain.OrderInfo;
import org.apache.ibatis.annotations.*;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/16 14:56
 */

@Mapper
public interface OrderDao {

    @Select("select * from miaosha_order where user_id = #{userId} and goods_id = #{goodsId}")
    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId") long goodsId);


    @Insert("insert into order_info(user_id, goods_id, delivery_addr_id, goods_name, goods_count , goods_price, order_channel,create_date,status) " +
            "value(#{userId},#{goodsId},#{deliveryAddrId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{createDate},#{status})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    long insert(OrderInfo orderInfo);


    @Insert("insert into miaosha_order(user_id, order_id, goods_id) value(#{userId},#{orderId},#{goodsId})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = long.class,before = false,statement = "select last_insert_id()")
    long insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

    @Select("select * from order_info where id = #{orderId}")
    OrderInfo getOrderById(long orderId);
}
