package com.atguigu.gmall.order.receiver;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.order.service.OrderService;
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
import java.util.HashMap;
import java.util.Map;

@Component
public class OrderReceiver {

    @Autowired
    OrderService orderService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(MqConst.EXCHANGE_DIRECT_PAYMENT_PAY),
            key = {MqConst.ROUTING_PAYMENT_PAY},
            value = @Queue(MqConst.QUEUE_PAYMENT_PAY)
    ))
    public void a(Channel channel ,Message message, String jsonMap) throws IOException {
        Map<String,Object> map = new HashMap<>();
        map = JSON.parseObject(jsonMap,map.getClass());
        // 将订单状态修改为已支付
        orderService.updateOrder(map);
        // 确认消费
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
