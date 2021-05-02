package com.atguigu.gmall.payment.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.payment.service.PaymentService;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PaymentReceiver {

    @Autowired
    PaymentService paymentService;

    @SneakyThrows
    @RabbitListener(queues = "queue.pay.check")
    public void a(Channel channel, Message message, String orderJson) throws IOException {

        OrderInfo orderInfo = JSON.parseObject(orderJson, OrderInfo.class);
        System.out.println(orderInfo.getOutTradeNo());
        System.out.println("延迟某长度时间");

        // 调用检查支付结果的服务

        Long count = orderInfo.getCount();
        System.out.println("检查队列剩余次数："+count);
        if (count <= 7) {
            orderInfo.setCount(count+1);
            // 如果查询结果是交易未创建，或者wati_buyer_pay，继续延迟检查
            paymentService.sendPayCheckQueue(orderInfo);
        }


        // 如果检查结果成功，进行支付修改和订单队列之前，需要幂等性检查
        String status = paymentService.getPayStatus(orderInfo.getOutTradeNo());

        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

}
