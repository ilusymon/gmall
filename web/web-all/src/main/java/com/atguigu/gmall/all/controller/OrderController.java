package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderController {

//    @Autowired
//    OrderFeignClient orderFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    OrderFeignClient orderFeignClient;

    @Autowired
    CartFeignClient cartFeignClient;

    @RequestMapping("trade.html")
    public String trade(HttpServletRequest request, Model model) {

        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }
        // 调用结算服务
        // 单点详情数据
        List<CartInfo> cartInfos = cartFeignClient.cartListInner();
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (CartInfo cartInfo : cartInfos) {
            // 用户isChecked选中的购物车商品才能够提交结算
            BigDecimal bigDecimalChecked = new BigDecimal("1");
            BigDecimal bigDecimalCart = new BigDecimal(cartInfo.getIsChecked() + "");
            int i = bigDecimalCart.compareTo(bigDecimalChecked);
            if (i == 0) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum());
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                orderDetails.add(orderDetail);
            }
        }
        // 收货信息数据
        List<UserAddress> userAddresses = userFeignClient.findUserAddressListByUserId(userId);


        // 生成一个订单交易码,页面放一个，后台服务放一个
        String tradeNo = orderFeignClient.genTradeNo();

        model.addAttribute("tradeNo", tradeNo);
        model.addAttribute("userAddressList", userAddresses);
        model.addAttribute("detailArrayList", orderDetails);
        model.addAttribute("totalAmount", getTotalAmount(cartInfos));


        return "order/trade";
    }

    private BigDecimal getTotalAmount(List<CartInfo> cartInfos) {

        BigDecimal totalAmount = new BigDecimal("0");

        if (null != cartInfos && cartInfos.size() > 0) {
            for (CartInfo cartInfo : cartInfos) {
                if (cartInfo.getIsChecked() == 1) {
                    totalAmount = totalAmount.add(cartInfo.getCartPrice());
                }
            }

        }

        return totalAmount;
    }

    @RequestMapping("myOrder.html")
    public String myOrder(HttpServletRequest request) {

        String userId = request.getHeader("userId");// 通过sso系统和网关的鉴权拦截器处理结果
        if (StringUtils.isEmpty(userId)) {
            userId = request.getHeader("userTempId");
        }

        userFeignClient.ping();

        return "order/myOrder";
    }


}
