package com.atguigu.gmall.payment.controller;


import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.payment.service.PaymentService;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/payment")
public class PayApiController {

    @Autowired
    PaymentService paymentService;

    @Autowired
    OrderFeignClient orderFeignClient;

    @RequestMapping("alipay/query")
    public Result alipayQuery(HttpServletRequest request,String outTradeNo){
        Map<String,Object> status = paymentService.alipayQuery(outTradeNo);

        return Result.ok(status);
    }


    @RequestMapping("alipay/callback/return")
    public String alipayCallback(HttpServletRequest request){

        PaymentInfo paymentInfo = new PaymentInfo();

        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String callback_content = request.getQueryString();

        paymentInfo.setTradeNo(trade_no);
        paymentInfo.setOutTradeNo(out_trade_no);
        paymentInfo.setCallbackTime(new Date());
        paymentInfo.setCallbackContent(callback_content);
        paymentInfo.setPaymentStatus(PaymentStatus.PAID.toString());

        // 幂等性检查

        // 接收支付宝回调信息，修改支付状态
        paymentService.updatePayment(paymentInfo);
        return "<form  action=\"http://payment.gmall.com/payment/success.html\">\n" +
                "</form>\n" +
                "<script>document.forms[0].submit();</script>";
    }

    @RequestMapping("alipay/submit/{orderId}")
    public String alipayTradePagePay(@PathVariable("orderId") Long orderId){

        // 返回一个支付页面
        String form = paymentService.alipayTradePagePay(orderId);
        System.out.println(form);


        // 发送一个延迟检查支付情况的消息队列，并且循环执行
        OrderInfo orderById = orderFeignClient.getOrderById(orderId);
        paymentService.sendPayCheckQueue(orderById);

        return form;

    }

}
