package com.atguigu.gmall.order.controller;


import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    OrderService orderService;

    @RequestMapping("getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") Long orderId){

        return orderService.getOrderById(orderId);
    }


    @RequestMapping("genTradeNo")
    String genTradeNo(HttpServletRequest request) {
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果

        String tradeNo = orderService.genTradeNo(userId);

        return tradeNo;
    }


    @RequestMapping("/auth/submitOrder")
    public Result submitOrder(String tradeNo, @RequestBody OrderInfo order, HttpServletRequest request) {


        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果

        // 验证tradeNo
        boolean b = orderService.checkTradeNo(userId, tradeNo);

        if (b) {
            // 保存订单信息,返回orderId
            order.setUserId(Long.parseLong(userId));
            String orderId = orderService.save(order);
            if (null != orderId) {
                return Result.ok(orderId);
            } else {
                return Result.fail();
            }
        } else {
            return Result.fail();
        }


    }


}
