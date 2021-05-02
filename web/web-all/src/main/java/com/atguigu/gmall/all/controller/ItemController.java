package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.item.client.ItemFeignClient;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;

    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model, HttpServletRequest request, HttpSession session){

        System.out.println(request.getRemoteAddr()+"同学开始访问商品详情。。。");

        // 调用后端服务，查询sku数据
        // sku信息，图片信息，销售属性列表，分类信息，价格信息

        Map<String,Object> map = itemFeignClient.getItem(skuId);

        Map<String,Object> o = (Map<String,Object>)map.get("skuInfo");
        String spuSaleAttrs = (String)map.get("valuesSkuJson");
        Integer spuId = (Integer)o.get("spuId");
        File file = new File("d:/spu_"+spuId+".json");

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(spuSaleAttrs.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        model.addAllAttributes(map);

        return "item/index";
    }


    @RequestMapping("test")
    public String test(Model model, HttpServletRequest request, HttpSession session){

        model.addAttribute("hello","hello thymeleaf !");

        request.setAttribute("requestValue","hello request");
        session.setAttribute("sessionValue","hello session");

        List<String> list = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            list.add("元素"+i);
        }

        model.addAttribute("list",list);

        model.addAttribute("obj","再见");
        return "test";
    }

}
