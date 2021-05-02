package com.atguigu.gmall.mq.service.impl;

import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RabbitServiceImpl implements RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }

    @Override
    public void sendDeadMessage(String exchange, String routingKey, String message, int time, TimeUnit timeUtil) {

        rabbitTemplate.convertAndSend(exchange,routingKey,message,processer->{
            // 设置消息的存活时间
            processer.getMessageProperties().setExpiration(1*1000*5+"");// ttl时间，默认毫秒

            return processer;
        });
    }

    @Override
    public void sendDelayMessage(String exchange, String routingKey, String message, int time, TimeUnit seconds) {
        rabbitTemplate.convertAndSend(exchange,routingKey,message,processer->{

            // 设置消息的延迟时间
            processer.getMessageProperties().setDelay(1*1000*time);// ttl时间，默认毫秒

            return processer;
        });
    }


}
