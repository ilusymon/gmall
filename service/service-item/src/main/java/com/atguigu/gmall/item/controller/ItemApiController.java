package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/item")
public class ItemApiController {

    @Autowired
    ItemService itemService;

    /**
     * 获取sku详情信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("{skuId}")
    public Map<String,Object> getItem(@PathVariable Long skuId) {

        Map<String,Object> map = new HashMap();

        map = itemService.getItem(skuId);

        return map;
    }


}
