package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.service.TrademarkService;
import com.atguigu.gmall.result.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping("admin/product/")
public class TrademarkController {

    @Autowired
    TrademarkService trademarkService;

    @RequestMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList(){

        List<BaseTrademark> baseTrademarks= trademarkService.getTrademarkList();

        return Result.ok(baseTrademarks);
    }

    @RequestMapping("baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable("page") Long page, @PathVariable("limit") Long limit){

        IPage<BaseTrademark> iPage = trademarkService.baseTrademark(page,limit);

        return Result.ok(iPage);

    }

}
