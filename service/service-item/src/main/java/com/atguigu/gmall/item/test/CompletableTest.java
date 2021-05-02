package com.atguigu.gmall.item.test;

import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CompletableTest {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 根据商品信息查询相关的属性和价格信息
        // 属性必须获得spuId后才能查询到
        // 价格信息只需要skuId就能查询到
        Map<String, Object> result = new HashMap<>();

        Long skuId = 22l;

        // 查询skuInfo，得到spuId
        CompletableFuture<SkuInfo> completableFutureSku = CompletableFuture.supplyAsync(new Supplier<SkuInfo>() {
            @Override
            public SkuInfo get() {
                SkuInfo skuInfo = new SkuInfo();
                skuInfo.setId(skuId);
                skuInfo.setSpuId(8l);
                System.out.println("根据skuId获得skuInfo");
                result.put("skuInfo",skuInfo);
                return skuInfo;
            }
        });

        // 根据spuId查询属性信息
        CompletableFuture<Void> completableFutureAttr = completableFutureSku.thenAcceptAsync(new Consumer<SkuInfo>() {
            @Override
            public void accept(SkuInfo skuInfo) {
                Long spuId = skuInfo.getSpuId();
                System.out.println("根据spuId获得属性");
                List<SpuSaleAttr> spuSaleAttrs = new ArrayList<>();
                result.put("spuSaleAttrs",spuSaleAttrs);
            }
        });

        // 查询价格信息
        CompletableFuture completableFuturePrice = CompletableFuture.runAsync(new Runnable() {
            @Override
            public void run() {
                System.out.println("根据skuId获得价格");
                result.put("price",100l);
            }
        });

//        SkuInfo skuInfo = completableFutureSku.get();
//
//        Void aVoid = completableFutureAttr.get();
//
//        Object o = completableFuturePrice.get();

        CompletableFuture.allOf(completableFutureSku,completableFutureAttr,completableFuturePrice).join();

        System.out.println("主线程执行完毕:"+result);


    }

    private static void b() throws InterruptedException, ExecutionException {
        CompletableFuture<Long> price1 = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("获得100");
                int i = 1 / 0;
                return 100l;
            }
        });

        CompletableFuture<Long> price2 = price1.thenApplyAsync(new Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                System.out.println("获得200");

                return aLong + 100l;
            }
        });

        CompletableFuture<Long> e1 = price2.exceptionally(new Function<Throwable, Long>() {

            @Override
            public Long apply(Throwable throwable) {
                System.out.println("第二条异常");
                return 0l;
            }
        });

        Thread.sleep(1000);

        CompletableFuture<Long> completableFuture = price2.whenComplete(new BiConsumer<Long, Throwable>() {
            @Override
            public void accept(Long o, Throwable throwable) {
                System.out.println("最终处理:" + o);
            }
        });

        Thread.sleep(2000);

        Long aLong = completableFuture.get();
    }

    private static void a() throws InterruptedException, ExecutionException {
        CompletableFuture<Long> price = CompletableFuture.supplyAsync(new Supplier<Long>() {
            @Override
            public Long get() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("获得100");
                int i = 1 / 0;
                return 100l;
            }
        }).exceptionally(new Function<Throwable, Long>() {
            @Override
            public Long apply(Throwable throwable) {
                // 第一条出错
                System.out.println("第一条异常");
                return 0l;
            }
        }).thenApplyAsync(new Function<Long, Long>() {
            @Override
            public Long apply(Long aLong) {
                System.out.println("获得200");

                return aLong + 101l;
            }
        }).exceptionally(new Function<Throwable, Long>() {
            @Override
            public Long apply(Throwable throwable) {
                System.out.println("第二条异常");
                return 100l;
            }
        }).whenComplete(new BiConsumer<Long, Throwable>() {
            @Override
            public void accept(Long aLong, Throwable throwable) {
                // 最终处理
                System.out.println("最终处理:" + aLong);
            }
        });


        Long aLong = price.get();
    }
}
