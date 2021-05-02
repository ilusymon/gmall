package com.atguigu.gmall.mq.service;

import java.util.concurrent.TimeUnit;

public interface RabbitService {
    void sendMessage(String exchange, String routingKey, String message);

    void sendDeadMessage(String exchange, String routingKey, String message, int i, TimeUnit seconds);

    void sendDelayMessage(String exchange, String routingKey, String message, int i, TimeUnit seconds);
}
