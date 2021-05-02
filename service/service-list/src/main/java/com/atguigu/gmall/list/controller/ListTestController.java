package com.atguigu.gmall.list.controller;


import com.atguigu.gmall.result.Result;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RequestMapping("list/test")
@RestController
public class ListTestController {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @RequestMapping("search")
    public Result test(){

        // 调用es高级检索api

        // 请求
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");


        // dsl语句封装
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(60);

        // bool must filter nested
        // aggs
        // hightlight

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id",287);
        MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title","神舟");
        boolQueryBuilder.must(matchQueryBuilder);
        boolQueryBuilder.filter(termQueryBuilder);
        searchSourceBuilder.query(boolQueryBuilder);

        String dsl = searchSourceBuilder.toString();
        System.out.println(dsl);
        searchRequest.source(searchSourceBuilder);

        // 返回结果
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            long totalHits = search.getHits().totalHits;

            System.out.println(totalHits);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return Result.ok();
    }


}
