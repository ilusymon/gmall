package com.atguigu.gmall.seckill.receiver;

import com.atguigu.gmall.mq.constant.MqConst;
import com.atguigu.gmall.seckill.service.SeckillService;
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
public class SeckillConsumer {

    @Autowired
    SeckillService seckillService;

    @SneakyThrows
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(value = MqConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = {MqConst.ROUTING_SECKILL_USER},
            value = @Queue(value = MqConst.QUEUE_SECKILL_USER)
    ))
    public void a(Channel channel, Message message, String userRecodeJson) throws IOException {

        System.out.println(userRecodeJson);

        seckillService.seckillOrderConsume(userRecodeJson);

        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }

}
