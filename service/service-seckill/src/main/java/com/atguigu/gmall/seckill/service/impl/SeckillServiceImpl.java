package com.atguigu.gmall.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.user.UserRecode;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.seckill.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.seckill.service.SeckillService;
import com.atguigu.gmall.seckill.util.CacheHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    RabbitService rabbitService;

    @Autowired
    SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public List<SeckillGoods> getSeckillGoods() {

        List<SeckillGoods> seckillGoods = (List<SeckillGoods>) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).values();//seckillGoodsMapper.selectList(null);

        return seckillGoods;
    }

    @Override
    public void putSeckillGoods() {

        List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectList(null);

        for (SeckillGoods seckillGood : seckillGoods) {
            Integer stockCount = seckillGood.getStockCount();

            for (int i = 0; i < stockCount; i++) {
                redisTemplate.opsForList().leftPush(RedisConst.SECKILL_STOCK_PREFIX + seckillGood.getSkuId(), seckillGood.getSkuId() + "");
            }

            redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, seckillGood.getSkuId() + "", seckillGood);

            // 发布商品入库的通知
            redisTemplate.convertAndSend("seckillpush", seckillGood.getSkuId() + ":" + "1");
        }

    }

    @Override
    public SeckillGoods getSeckillGood(Long skuId) {

        SeckillGoods seckillGood = (SeckillGoods) redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");

        return seckillGood;
    }

    @Override
    public void seckillOrder(String userId, Long skuId) {


        UserRecode userRecode = new UserRecode();
        userRecode.setSkuId(skuId);
        userRecode.setUserId(userId);
        // 削峰，请求进入缓冲区
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_SECKILL_USER, MqConst.ROUTING_SECKILL_USER, JSON.toJSONString(userRecode));


    }

    @Override
    public void seckillOrderConsume(String userRecodeJson) {


        UserRecode userRecode = JSON.parseObject(userRecodeJson, UserRecode.class);

        // 判断此时的抢购服务器的状态(某个skuId的)
        String status = (String) CacheHelper.get(userRecode.getSkuId() + "");

        // 抢库存
        String skuId = (String)redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX + userRecode.getSkuId() + "");

        if(!StringUtils.isEmpty(skuId)){
            // 生成预订单，放入缓存
            OrderRecode orderRecode = new OrderRecode();
            orderRecode.setUserId(userRecode.getUserId());
            SeckillGoods seckillGoods = (SeckillGoods)redisTemplate.boundHashOps(RedisConst.SECKILL_GOODS).get(skuId + "");
            orderRecode.setSeckillGoods(seckillGoods);
            orderRecode.setNum(1);

            redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).put(userRecode.getUserId(),orderRecode);
        }else {
            // 库存售罄，发布通知，修改服务器状态
            redisTemplate.convertAndSend("seckillpush",userRecode.getSkuId()+":"+"0");
        }
    }

    @Override
    public Boolean lockSeckillUser(String userId) {

        return redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER+userId+":lock", "1", 30, TimeUnit.SECONDS);
    }

    /**
     * 查看是否生成了正式订单
     * @param userId
     * @return
     */
    @Override
    public String getOrderStr(String userId) {

        String orderId = (String)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS_USERS).get(userId);

        return orderId;
    }

    /***
     * 查看是否生成了预订单
     * @param userId
     * @return
     */
    @Override
    public boolean getOrderRecode(String userId) {
        boolean b = false;

        Object o = redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);

        if(null!=o){
            b = true;
        }


        return b;
    }

    @Override
    public OrderRecode getOrderRecodeForOrder(String userId) {

        OrderRecode o = (OrderRecode)redisTemplate.boundHashOps(RedisConst.SECKILL_ORDERS).get(userId);

        return o;
    }
}
