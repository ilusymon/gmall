package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.SpuService;
import com.atguigu.gmall.product.service.TrademarkService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("admin/product/")
public class SpuController {

    @Autowired
    SpuService spuService;

    @RequestMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId){

        List<SpuSaleAttr> spuSaleAttrs = spuService.spuSaleAttrList(spuId);

        return Result.ok(spuSaleAttrs);

    }


    @RequestMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId){

        List<SpuImage> spuImages = spuService.spuImageList(spuId);

        return Result.ok(spuImages);

    }

    @RequestMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo){

        spuService.saveSpuInfo(spuInfo);

        return Result.ok();

    }

    @RequestMapping("baseSaleAttrList")
    public Result baseSaleAttrList(){

        List<BaseSaleAttr> baseSaleAttrs = spuService.baseSaleAttrList();

        return Result.ok(baseSaleAttrs);

    }


    @RequestMapping("{page}/{limit}")
    public Result spuList(@PathVariable("page") Long page, @PathVariable("limit") Long limit,Long category3Id){

        IPage<SpuInfo> iPage = spuService.spuList(page,limit,category3Id);
        Result<IPage<SpuInfo>> ok = Result.ok(iPage);

        return Result.ok(iPage);

    }

}
