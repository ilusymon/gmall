package com.atguigu.gmall.seckill.service;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillService {
    List<SeckillGoods> getSeckillGoods();

    void putSeckillGoods();

    SeckillGoods getSeckillGood(Long skuId);

    void seckillOrder(String userId, Long skuId);

    void seckillOrderConsume(String userRecodeJson);

    Boolean lockSeckillUser(String userId);

    String getOrderStr(String userId);

    boolean getOrderRecode(String userId);

    OrderRecode getOrderRecodeForOrder(String userId);
}
