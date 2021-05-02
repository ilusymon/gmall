package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.ware.OrderDetail;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.seckill.client.SeckillFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import com.atguigu.gmall.util.MD5;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
public class SeckillController {


    @Autowired
    SeckillFeignClient seckillFeignClient;

    @Autowired
    UserFeignClient userFeignClient;

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping("seckill/trade.html")
    public String trade(Model model, HttpServletRequest request) {

        String userId = request.getHeader("userId");

        OrderRecode recode = seckillFeignClient.getOrderRecode(userId);

        Long skuId = recode.getSeckillGoods().getSkuId();

        List<UserAddress> userAddresses = userFeignClient.findUserAddressListByUserId(userId);
        SkuInfo skuInfoById = productFeignClient.getSkuInfoById(skuId);
        List<OrderDetail> orderDetails = new ArrayList<>();
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setImgUrl(skuInfoById.getSkuDefaultImg());
        orderDetail.setOrderPrice(skuInfoById.getPrice());
        orderDetail.setSkuId(skuInfoById.getId()+"");
        orderDetail.setSkuName(skuInfoById.getSkuName());
        orderDetail.setSkuNum(1);
        orderDetails.add(orderDetail);


        model.addAttribute("userAddressList",userAddresses);

        model.addAttribute("detailArrayList",orderDetails);
        return "seckill/trade";


    }


    @RequestMapping("/seckill/queue.html")
    public String queue(Model model, Long skuId, String skuIdStr, HttpServletRequest request) {

        String userId = request.getHeader("userId");

        String seckillCode = MD5.encrypt(userId + skuId);

        if (skuIdStr.equals(seckillCode)) {
            model.addAttribute("skuId", skuId);
            model.addAttribute("skuIdStr", skuIdStr);
            return "seckill/queue";
        } else {
            model.addAttribute("message", "请求不合法");
            return "seckill/fail";
        }

    }


    @RequestMapping("/seckill/{skuId}.html")
    public String getSeckillGood(Model model, @PathVariable("skuId") Long skuId) {

        SeckillGoods seckillGood = seckillFeignClient.getSeckillGood(skuId);

        model.addAttribute("item", seckillGood);
        return "seckill/item";
    }

    /**
     * 秒杀列表
     *
     * @param model
     * @return
     */
    @RequestMapping("seckill.html")
    public String index(Model model) {

        List<SeckillGoods> seckillGoods = seckillFeignClient.getSeckillGoods();

        model.addAttribute("list", seckillGoods);
        return "seckill/index";
    }

}
