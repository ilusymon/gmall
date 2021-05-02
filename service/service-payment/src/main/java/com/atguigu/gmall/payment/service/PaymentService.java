package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    String alipayTradePagePay(Long orderId);

    void updatePayment(PaymentInfo paymentInfo);

    Map<String,Object> alipayQuery(String outTradeNo);

    void sendPayCheckQueue(OrderInfo orderById);

    String getPayStatus(String outTradeNo);
}
