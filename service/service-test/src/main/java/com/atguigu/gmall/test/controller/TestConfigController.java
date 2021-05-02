package com.atguigu.gmall.test.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestConfigController {

    @Value("${myName}")
    String myName ;

    @RequestMapping("test01")
    public String test01(){

        return "name="+myName;
    }

}
