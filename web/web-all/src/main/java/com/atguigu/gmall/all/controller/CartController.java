package com.atguigu.gmall.all.controller;


import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class CartController {

    @Autowired
    CartFeignClient cartFeignClient;

    @RequestMapping("cart/cart.html")
    public String cartList(Model model, HttpServletRequest request){

        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        return "cart/index";
    }

    @RequestMapping("addCart.html")
    public String addCart(Long skuId, Long skuNum){

        // 调用购物车服务，添加购物车信息
        cartFeignClient.addCart(skuId,skuNum);

        return "redirect:http://cart.gmall.com/cart/addCartSuccess.html?skuName=?&price=?";// 静态传参
    }

}
