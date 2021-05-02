package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.test.client.TestFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    TestFeignClient testFeignClient;

    @RequestMapping("testWeb")
    public String testWeb(){

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {

        }

        System.out.println(Thread.currentThread().getName());

        System.out.println("调用test客户端");

        String pong = testFeignClient.ping();

        return "testWeb:"+pong;
    }
}
