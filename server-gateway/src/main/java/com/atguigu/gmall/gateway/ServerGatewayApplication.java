package com.atguigu.gmall.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableFeignClients("com.atguigu.gmall")
@ComponentScan("com.atguigu.gmall")
@EnableDiscoveryClient
public class ServerGatewayApplication {
    public static void main(String[] args) {

        SpringApplication.run(ServerGatewayApplication.class, args);
    }
}
