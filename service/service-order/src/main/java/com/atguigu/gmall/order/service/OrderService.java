package com.atguigu.gmall.order.service;

import com.atguigu.gmall.model.order.OrderInfo;

import java.util.Map;

public interface OrderService {
    String save(OrderInfo order);

    String genTradeNo(String userId);

    boolean checkTradeNo(String userId, String tradeNo);

    OrderInfo getOrderById(Long orderId);

    void updateOrder(Map<String, Object> map);
}
