package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseSaleAttr;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.product.service.SpuService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("admin/product/")
public class SkuController {

    @Autowired
    SkuService skuService;


    @RequestMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId){
        skuService.onSale(skuId);
        return Result.ok();
    }


    @RequestMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId){
        skuService.cancelSale(skuId);
        return Result.ok();
    }


    @RequestMapping("list/{page}/{limit}")
    public Result skuList(@PathVariable("page") Long page, @PathVariable("limit") Long limit){

        IPage<SkuInfo> iPage = skuService.skuList(page,limit);

        return Result.ok(iPage);

    }


    @RequestMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo){

        skuService.saveSkuInfo(skuInfo);

        return Result.ok();

    }



}
