package com.atguigu.gmall.test.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(value = "service-test",fallback = TestDegradeFeignClient.class)
public interface TestFeignClient {

    @RequestMapping("api/test/ping")
    public String ping();
}
