package com.atguigu.gmall.mq.controller;


import com.atguigu.gmall.mq.service.RabbitService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class MqTestController {

    @Autowired
    RabbitService rabbitService;

    @RequestMapping("testMq")
    public String testMq(){

        rabbitService.sendMessage("a","b","cccc");

        return "testMq";
    }

    @RequestMapping("testDeadMq")
    public String testDeadMq(){

        rabbitService.sendDeadMessage("exchange.dead","routing.dead.1","cccc",10, TimeUnit.SECONDS);

        return "testDeadMq";
    }

    @RequestMapping("testDelayMq")
    public String testDelayMq(){

        rabbitService.sendDelayMessage("exchange.delay","routing.delay","cccc",7, TimeUnit.SECONDS);

        return "testDelayMq";
    }


}
