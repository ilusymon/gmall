package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    ListFeignClient listFeignClient;

    @Override
    public Map<String, Object> getItem(Long skuId) {

        long currentTimeMillisStart = System.currentTimeMillis();

        Map<String, Object> result = getItemThread(skuId);

        long currentTimeMillisEnd = System.currentTimeMillis();

        System.out.println("执行总时间："+(currentTimeMillisEnd - currentTimeMillisStart));

        // 更新商品的热度值
        listFeignClient.hotScore(skuId);

        return result;
    }

    private Map<String,Object> getItemThread(Long skuId) {
        Map<String, Object> result = new HashMap<>();

        // skuInfo // image
        CompletableFuture<SkuInfo> completableFutureSku = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
                result.put("skuInfo",skuInfo);// 封装数据
                return skuInfo;
            }
        },threadPoolExecutor);


        // 分类信息
        CompletableFuture completableFutureCategoryView = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
                result.put("categoryView",baseCategoryView);// 封装数据
            }
        },threadPoolExecutor);


        // 销售属性列表
        CompletableFuture completableFutureSaleAttrs = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<SpuSaleAttr> spuSaleAttrs =  productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(),skuId);
                result.put("spuSaleAttrList",spuSaleAttrs);// 封装数据
            }
        },threadPoolExecutor);



        // 价格
        CompletableFuture completableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                BigDecimal price = productFeignClient.getSkuPriceById(skuId);
                result.put("price",price);// 封装数据
            }
        },threadPoolExecutor);


        // 销售属性组合hash表
        CompletableFuture completableFutureSkuMaps = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                List<Map<String,Object>> valuesSkuMaps = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());
                // 将mybatis的map集合转为一个map
                Map<String,Object> map = new HashMap<>();
                for (Map<String, Object> valuesSkuMap : valuesSkuMaps) {
                    map.put((String)valuesSkuMap.get("valueIds"),valuesSkuMap.get("sku_id"));
                }
                result.put("valuesSkuJson", JSON.toJSONString(map));// 封装数据
            }
        },threadPoolExecutor);

        CompletableFuture.allOf(completableFutureSku,completableFutureSaleAttrs,completableFutureCategoryView,completableFuturePrice,completableFutureSkuMaps).join();

        return result;
    }

    private Map<String, Object> getItemSingle(Long skuId) {
        Map<String, Object> result = new HashMap<>();

        // skuInfo // image
        SkuInfo skuInfo = productFeignClient.getSkuInfoById(skuId);
        result.put("skuInfo",skuInfo);

        // 分类信息
        BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        result.put("categoryView",baseCategoryView);

        // 销售属性列表
        List<SpuSaleAttr> spuSaleAttrs =  productFeignClient.getSpuSaleAttrListCheckBySku(skuInfo.getSpuId(),skuId);
        result.put("spuSaleAttrList",spuSaleAttrs);


        // 价格
        BigDecimal price = productFeignClient.getSkuPriceById(skuId);
        result.put("price",price);

        // 销售属性组合hash表
        List<Map<String,Object>> valuesSkuMaps = productFeignClient.getSaleAttrValuesBySpu(skuInfo.getSpuId());
        // 将mybatis的map集合转为一个map
        Map<String,Object> map = new HashMap<>();
        for (Map<String, Object> valuesSkuMap : valuesSkuMaps) {
            map.put((String)valuesSkuMap.get("valueIds"),valuesSkuMap.get("sku_id"));
        }
        result.put("valuesSkuJson", JSON.toJSONString(map));
        return result;
    }
}
