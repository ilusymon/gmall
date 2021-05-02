package com.atguigu.gmall.order.client;


import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "service-order")
public interface OrderFeignClient {



    @RequestMapping("api/order/genTradeNo")
    String genTradeNo();

    @RequestMapping("api/order/getOrderById/{orderId}")
    OrderInfo getOrderById(@PathVariable("orderId") Long orderId);
}
