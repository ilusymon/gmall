package com.atguigu.gmall.item.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor a(){

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(50, 100, 3, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(5000));

        return threadPoolExecutor;
    }

}
