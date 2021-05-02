package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.mq.service.RabbitService;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Autowired
    RabbitService rabbitService;

    @Override
    public String alipayTradePagePay(Long orderId) {

        OrderInfo orderById = orderFeignClient.getOrderById(orderId);

        // 公共参数
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        request.setReturnUrl(AlipayConfig.return_payment_url);

        // 请求参数
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",orderById.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject",orderById.getOrderDetailList().get(0).getSkuName());
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradePagePayResponse response = null;
        try {
            response = alipayClient.pageExecute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
        } else {
            System.out.println("调用失败");
        }

        // 保存支付信息
        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",orderById.getOutTradeNo());
        PaymentInfo paymentInfoIfExist = paymentInfoMapper.selectOne(queryWrapper);

        if(null==paymentInfoIfExist){
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setOutTradeNo(orderById.getOutTradeNo());
            paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.toString());
            paymentInfo.setSubject(orderById.getOrderDetailList().get(0).getSkuName());
            paymentInfo.setCreateTime(new Date());
            paymentInfo.setTotalAmount(orderById.getTotalAmount());
            paymentInfo.setOrderId(orderId);
            paymentInfo.setPaymentType(PaymentType.ALIPAY.getComment());
            paymentInfoMapper.insert(paymentInfo);
        }



        return response.getBody();
    }

    @Override
    public void updatePayment(PaymentInfo paymentInfo) {

        // 根据回调的out_trade_no修改支付状态

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("out_trade_no",paymentInfo.getOutTradeNo());
        paymentInfoMapper.update(paymentInfo,queryWrapper);

        paymentInfo = paymentInfoMapper.selectOne(queryWrapper);

        // 发送支付成功通知，由订单系统消费，订单状态修改为已支付
        Map<String,Object> message = new HashMap<>();
        message.put("out_trade_no",paymentInfo.getOutTradeNo());
        message.put("order_id",paymentInfo.getOrderId());
        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY,MqConst.ROUTING_PAYMENT_PAY,JSON.toJSONString(message));


    }

    @Override
    public Map<String, Object> alipayQuery(String outTradeNo) {

        Map<String, Object> result = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> map = new HashMap<>();
        map.put("out_trade_no",outTradeNo);
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        boolean success = response.isSuccess();
        if(response.isSuccess()){
            System.out.println("调用成功");
            result.put("status",response.getTradeStatus());
        } else {
            System.out.println("调用失败");
            result.put("status","调用失败");
        }

        return result;
    }

    @Override
    public void sendPayCheckQueue(OrderInfo orderById) {


        rabbitService.sendDelayMessage("exchange.pay.check","routing.pay.check",JSON.toJSONString(orderById),3, TimeUnit.SECONDS);

    }

    @Override
    public String getPayStatus(String outTradeNo) {

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("out_trade_no",outTradeNo);

        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(queryWrapper);

        String paymentStatus = paymentInfo.getPaymentStatus();

        return paymentStatus;



    }
}
