package com.atguigu.gmall.seckill.controller;


import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.result.ResultCodeEnum;
import com.atguigu.gmall.seckill.service.SeckillService;
import com.atguigu.gmall.seckill.util.CacheHelper;
import com.atguigu.gmall.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/activity")
public class SeckillApiController {

    @Autowired
    SeckillService seckillService;

    @RequestMapping("seckill/auth/checkOrder/{skuId}")
    Result checkOrder(@PathVariable("skuId") Long skuId, HttpServletRequest request){

        String userId = request.getHeader("userId");

        //已下单成功
        String orderId = seckillService.getOrderStr(userId);
        if(!StringUtils.isEmpty(orderId)){
            return Result.build(orderId,ResultCodeEnum.SECKILL_ORDER_SUCCESS);
        }

        //去下单
        boolean b = seckillService.getOrderRecode(userId);
        if(b){
            return Result.build(null,ResultCodeEnum.SECKILL_SUCCESS);
        }

        //库存售罄
        String status = (String)CacheHelper.get(skuId+"");
        if(null==status||status.equals("0")){
            return Result.build(null,ResultCodeEnum.SECKILL_FINISH);
        }

        //正在排队
        return Result.build(null,ResultCodeEnum.SECKILL_RUN);
    }

    @RequestMapping("seckill/getOrderRecode/{userId}")
    OrderRecode getOrderRecode(@PathVariable("userId") String userId){

        OrderRecode orderRecode = seckillService.getOrderRecodeForOrder(userId);

        return orderRecode;

    }


    @RequestMapping("seckill/auth/seckillOrder/{skuId}")
    Result seckillOrder(@PathVariable("skuId") Long skuId, String skuIdStr ,HttpServletRequest request){

        // 判断此时的抢购服务器的状态(某个skuId的)
        String status = (String)CacheHelper.get(skuId + "");

        String userId = request.getHeader("userId");

        // 判断抢购码
        String seckillCode = MD5.encrypt(userId + skuId);


        // 削峰操作，发送抢购请求消息队列
        // 限制用户的抢购频率
        Boolean aBoolean = seckillService.lockSeckillUser(userId);

        if(aBoolean){
            seckillService.seckillOrder(userId,skuId);
            return Result.ok();
        }else {
            return Result.fail();
        }


    }


    @RequestMapping("seckill/auth/getSeckillSkuIdStr/{skuId}")
    Result getSeckillSkuIdStr(@PathVariable("skuId") Long skuId, HttpServletRequest request){

        // 判断此时的抢购服务器的状态(某个skuId的)
        String status = (String)CacheHelper.get(skuId + "");

        String userId = request.getHeader("userId");

        // 生成抢购码
        String seckillCode = MD5.encrypt(userId + skuId);

        return Result.ok(seckillCode);
    }


    @RequestMapping("seckill/getSeckillGood/{skuId}")
    SeckillGoods getSeckillGood(@PathVariable("skuId") Long skuId){

        SeckillGoods seckillGood = seckillService.getSeckillGood(skuId);

        return seckillGood;
    }


    @RequestMapping("seckill/getSkuStatus/{skuId}")
    Result getSkuStatus(@PathVariable("skuId") Long skuId){
        String skuStatus = (String)CacheHelper.get(skuId + "");
        Map<String,String> map = new HashMap<>();
        map.put(skuId+"",skuStatus);
        return Result.ok(map);
    }


    @RequestMapping("seckill/putSeckillGoods")
    Result putSeckillGoods(){

        seckillService.putSeckillGoods();

        return Result.ok();

    }

    @RequestMapping("seckill/getSeckillGoods")
    List<SeckillGoods> getSeckillGoods(){

        List<SeckillGoods> seckillGoods = seckillService.getSeckillGoods();

        return seckillGoods;

    }

}
