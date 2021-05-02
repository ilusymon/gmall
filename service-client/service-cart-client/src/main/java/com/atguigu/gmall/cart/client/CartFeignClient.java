package com.atguigu.gmall.cart.client;

import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@FeignClient(value = "service-cart")
public interface CartFeignClient {

    @RequestMapping("api/cart/addCart/{skuId}/{skuNum}")
    void addCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum")  Long skuNum);

    @RequestMapping("api/cart/cartList")
    Result cartList();

    @RequestMapping("api/cart/cartListInner")
    List<CartInfo> cartListInner();
}
