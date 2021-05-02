package com.atguigu.gmall.product.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.AttrService;
import com.atguigu.gmall.product.service.CategoryService;
import com.atguigu.gmall.product.service.SkuService;
import com.atguigu.gmall.product.service.TrademarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("api/product/")
public class ProductApiController {


    @Autowired
    SkuService skuService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    TrademarkService trademarkService;

    @Autowired
    AttrService attrService;

    @RequestMapping("getSkuInfoById/{skuId}")
    SkuInfo getSkuInfoById(@PathVariable("skuId") Long skuId) {
        SkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        return skuInfo;
    }

    @RequestMapping("getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {

        BaseCategoryView baseCategoryView = categoryService.getCategoryView(category3Id);

        return baseCategoryView;
    }

    @RequestMapping("getSkuPriceById/{skuId}")
    BigDecimal getSkuPriceById(@PathVariable("skuId") Long skuId) {
        BigDecimal price = skuService.getSkuPriceById(skuId);

        return price;
    }


    @RequestMapping("getSpuSaleAttrListCheckBySku/{spuId}/{skuId}")
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("spuId") Long spuId, @PathVariable("skuId") Long skuId) {

        List<SpuSaleAttr> spuSaleAttrs = skuService.getSpuSaleAttrListCheckBySku(spuId,skuId);

        return spuSaleAttrs;
    }

    @RequestMapping("getSaleAttrValuesBySpu/{spuId}")
    List<Map<String, Object>> getSaleAttrValuesBySpu(@PathVariable("spuId") Long spuId){

        List<Map<String, Object>> maps = skuService.getSaleAttrValuesBySpu(spuId);

        return maps;
    }

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){
        List<JSONObject> jsonObjects = categoryService.getBaseCategoryList();

        return jsonObjects;
    }

    @RequestMapping("getTrademarkById/{tmId}")
    BaseTrademark getTrademarkById(@PathVariable("tmId") Long tmId){
        BaseTrademark baseTrademark = trademarkService.getTrademarkById(tmId);

        return baseTrademark;
    }

    @RequestMapping("getSearchAttr/{skuId}")
    List<SearchAttr> getSearchAttr(@PathVariable("skuId") Long skuId){
        List<SearchAttr> searchAttrs = skuService.getSearchAttr(skuId);

        return searchAttrs;
    }
}
