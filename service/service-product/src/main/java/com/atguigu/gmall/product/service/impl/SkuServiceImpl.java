package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.aspect.GmallCache;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import springfox.documentation.annotations.Cacheable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ListFeignClient listFeignClient;


    @Override
    public IPage<SkuInfo> skuList(Long page, Long limit) {
        Page<SkuInfo> pageParam = new Page<>();

        pageParam.setSize(limit);
        pageParam.setCurrent(page);

        IPage<SkuInfo> iPage = skuInfoMapper.selectPage(pageParam, null);

        return iPage;
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        // 保存sku，返回主键
        skuInfoMapper.insert(skuInfo);

        Long skuId = skuInfo.getId();

        // 保存sku图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }


        // 保存sku平台属性
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();

        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }


        // 保存sku销售属性

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }

    }

    @Override
    public void onSale(Long skuId) {

        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);

        // 同步搜索引擎
        listFeignClient.onSale(skuId);

    }

    @Override
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);

        // 同步搜索引擎
        listFeignClient.cancelSale(skuId);
    }

    @Override
    @GmallCache
    public SkuInfo getSkuInfoById(Long skuId) {

        SkuInfo skuInfo = null;

        skuInfo = getSkuInfoFromDb(skuId);

        return skuInfo;

    }


    public SkuInfo getSkuInfoByIdBak(Long skuId) {

        SkuInfo skuInfo = null;

        // 查询缓存
        skuInfo = (SkuInfo) redisTemplate.opsForValue().get("sku:" + skuId + ":info");

        if (null == skuInfo) {

            String lockTag = UUID.randomUUID().toString();
            Boolean OK = redisTemplate.opsForValue().setIfAbsent("sku:" + skuId + ":lock", lockTag,3, TimeUnit.SECONDS);

            if(OK){
                System.out.println("拿到分布式锁，访问db");

                // 无值，查询数据库
                skuInfo = getSkuInfoFromDb(skuId);

                if (null != skuInfo) {
                    // 同步缓存
                    redisTemplate.opsForValue().set("sku:" + skuId + ":info", skuInfo);
                }else {
                    redisTemplate.opsForValue().set("sku:" + skuId + ":info", null,1,TimeUnit.MINUTES);
                }


                // 用完分布式锁，释放
//                String delTag = (String)redisTemplate.opsForValue().get("sku:" + skuId + ":lock");
//                if(delTag.equals(lockTag)){
//                    redisTemplate.delete("sku:" + skuId + ":lock");
//                }

                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";// 脚本查询是否存在存在则删除否则返回0
                // 设置lua脚本返回的数据类型
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                // 设置lua脚本返回类型为Long
                redisScript.setResultType(Long.class);
                redisScript.setScriptText(script);
                Long execute = (Long)redisTemplate.execute(redisScript, Arrays.asList("sku:" + skuId + ":lock"), lockTag);// 执行脚本

            }else {
                // 没有查询到缓存数据，并且没有获得分布式锁
                // 自旋
                System.out.println("没有拿到分布式锁，开始自旋");
                return getSkuInfoById(skuId);
            }
        }else {
            System.out.println("从缓存中获得数据");
        }
        // 有值则直接return
        return skuInfo;

    }

    /***
     * 查询数据库
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoFromDb(Long skuId) {
        SkuInfo skuInfo;
        skuInfo = skuInfoMapper.selectById(skuId);
        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);
        skuInfo.setSkuImageList(skuImages);
        return skuInfo;
    }

    @Override
    public BigDecimal getSkuPriceById(Long skuId) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", skuId);
        SkuInfo skuInfo = skuInfoMapper.selectOne(queryWrapper);

        return skuInfo.getPrice();
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, Long skuId) {

        List<SpuSaleAttr> spuSaleAttrs = skuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(spuId, skuId);

        return spuSaleAttrs;
    }

    @Override
    public List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId) {

        List<Map<String, Object>> listMap = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);

        return listMap;
    }


    @Override
    public List<SearchAttr> getSearchAttr(Long skuId) {

        List<SearchAttr> searchAttrs = skuAttrValueMapper.selectSearchAttr(skuId);

        return searchAttrs;
    }
}
