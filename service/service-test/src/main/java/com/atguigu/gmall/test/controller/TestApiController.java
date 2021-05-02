package com.atguigu.gmall.test.controller;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/test")
@RestController
public class TestApiController {

    @RequestMapping("ping")
    public String ping(){

        // int i = 1/0;

        System.out.println("调用test服务器");

        return "testService";
    }
}
