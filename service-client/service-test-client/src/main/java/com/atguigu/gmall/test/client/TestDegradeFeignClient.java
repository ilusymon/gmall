package com.atguigu.gmall.test.client;

import org.springframework.stereotype.Component;

@Component
public class TestDegradeFeignClient implements TestFeignClient {
    @Override
    public String ping() {
        return "service-test服务出现问题，使用客户端降级服务";
    }
}
