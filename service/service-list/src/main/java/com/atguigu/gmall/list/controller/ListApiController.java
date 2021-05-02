package com.atguigu.gmall.list.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.result.Result;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    ListService listService;

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;

    @RequestMapping("list")
    SearchResponseVo list(@RequestBody SearchParam searchParam){
        SearchResponseVo searchResponseVo = new SearchResponseVo();

        searchResponseVo = listService.list(searchParam);

        return searchResponseVo;
    }

    @RequestMapping("onSale/{skuId}")
    void onSale(@PathVariable("skuId") Long skuId){
        listService.onSale(skuId);
    }

    @RequestMapping("cancelSale/{skuId}")
    void cancelSale(@PathVariable("skuId") Long skuId){
        listService.cancelSale(skuId);
    }

    @RequestMapping("createIndex")
    Result createIndex(){

        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);

        return Result.ok();
    }

    @RequestMapping("getBaseCategoryList")
    List<JSONObject> getBaseCategoryList(){

        // ElasticsearchRestTemplate;=redisTemplate
        // ElasticsearchRepository;=mybatisplus
        // RestHighLevelClient;=高级检索客户端

        List<JSONObject> jsonObjects = listService.getBaseCategoryList();

        return jsonObjects;
    }

    @RequestMapping("hotScore/{skuId}")
    void hotScore(@PathVariable("skuId") Long skuId){

        listService.hotScore(skuId);
    }
}
