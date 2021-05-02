package com.atguigu.gmall.mq.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class ProducerMqAskConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback{

    @Autowired
    RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        // 将确认类放入RabbitTemplate
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setConfirmCallback(this);
    }

    /**
     * 消息发送确认
     * @param correlationData
     * @param ack
     * @param error
     */
    @Override
    public void confirm(@Nullable CorrelationData correlationData, boolean ack, @Nullable String error) {

        System.out.println("消息的发送状态是："+ack+"异常信息是："+error);
    }

    /**
     *
     * @param message
     * @param code
     * @param codeMessage
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int code, String codeMessage, String exchange, String routingKey) {
        System.out.println("消息的投递状态是：");
    }
}
