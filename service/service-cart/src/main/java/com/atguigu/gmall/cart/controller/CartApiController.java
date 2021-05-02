package com.atguigu.gmall.cart.controller;


import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("api/cart")
public class CartApiController {

    @Autowired
    CartService cartService;


    @RequestMapping("checkCart/{skuId}/{isChecked}")
    Result checkCart(HttpServletRequest request, @PathVariable("skuId") Long skuId, @PathVariable("isChecked")  Integer isChecked){
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        CartInfo cartInfo = new CartInfo();

        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        cartInfo.setIsChecked(isChecked);

        cartService.checkCart(cartInfo);
        return Result.ok();
    }


    @RequestMapping("cartList")
    Result cartList(HttpServletRequest request){
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        List<CartInfo>  cartInfos = cartService.cartList(userId);
        return Result.ok(cartInfos);
    }

    @RequestMapping("cartListInner")
    List<CartInfo>  cartListInner(HttpServletRequest request){
        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        List<CartInfo>  cartInfos = cartService.cartList(userId);
        return cartInfos;
    }

    @RequestMapping("addCart/{skuId}/{skuNum}")
    void addCart(HttpServletRequest request,@PathVariable("skuId") Long skuId, @PathVariable("skuNum")  Long skuNum){

        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if(StringUtils.isEmpty(userId)){
            userId = request.getHeader("userTempId");
        }

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setSkuNum(skuNum.intValue());
        cartInfo.setUserId(userId);
        cartService.addCart(cartInfo);


    }

}
