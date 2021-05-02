package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.MultiCollectorManager;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public List<JSONObject> getBaseCategoryList() {

        List<JSONObject> jsonObjects = productFeignClient.getBaseCategoryList();

        return jsonObjects;
    }

    @Override
    public void onSale(Long skuId) {

        Goods goods = new Goods();

        // 根据skuId查询goods
        SkuInfo sku = productFeignClient.getSkuInfoById(skuId);

        // 品牌
        Long tmId = sku.getTmId();
        BaseTrademark baseTrademark = productFeignClient.getTrademarkById(tmId);

        // 分类
        Long category3Id = sku.getCategory3Id();
        BaseCategoryView categoryView = productFeignClient.getCategoryView(category3Id);


        // 属性集合
        List<SearchAttr> searchAttrs = productFeignClient.getSearchAttr(skuId);

        goods.setTitle(sku.getSkuName());
        goods.setHotScore(0l);
        goods.setId(skuId);
        goods.setAttrs(searchAttrs);
        goods.setTmName(baseTrademark.getTmName());
        goods.setTmLogoUrl(baseTrademark.getLogoUrl());
        goods.setPrice(sku.getPrice().doubleValue());
        goods.setTmId(baseTrademark.getId());
        goods.setDefaultImg(sku.getSkuDefaultImg());
        goods.setCreateTime(new Date());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory3Name(categoryView.getCategory3Name());

        // 将goods插入到es库
        goodsRepository.save(goods);
    }

    @Override
    public void cancelSale(Long skuId) {

        // 根据skuId删除goods
        goodsRepository.deleteById(skuId);

    }

    @Override
    public void hotScore(Long skuId) {

        // 更新redis，获得当前hotScore值
        Long increment = redisTemplate.opsForValue().increment("sku:" + skuId + ":hotScore", 1l);


        if (increment % 10 == 0) {
            // 说明已经更新了热度值1000次
            Optional<Goods> goodsOptional = goodsRepository.findById(skuId);
            Goods goods = goodsOptional.get();
            goods.setHotScore(increment);
            goodsRepository.save(goods);
        }


    }

    @Override
    public SearchResponseVo list(SearchParam searchParam) {

        // 封装语句
        SearchSourceBuilder searchSourceBuilder = getSearchQueryDsl(searchParam);


        // 执行查询
        // 返回结果
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        SearchResponseVo searchResponseVo = null;
        try {
            SearchResponse result = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            // 解析结果
            searchResponseVo = parseSearchResult(result);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return searchResponseVo;
    }

    private SearchResponseVo parseSearchResult(SearchResponse result) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();

        // 解析商品sku信息
        Set<Long> set = new HashSet<>();
        SearchHits hits = result.getHits();
        SearchHit[] hitsResult = hits.getHits();
        if (null != hitsResult && hitsResult.length > 0) {
            List<Goods> goodsList = new ArrayList<>();
            for (SearchHit documentFields : hitsResult) {
                String sourceAsString = documentFields.getSourceAsString();

                Goods goods = JSON.parseObject(sourceAsString, Goods.class);

                Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                if(null!=highlightFields&&highlightFields.size()>0){
                    HighlightField highlightField = highlightFields.get("title");
                    // 关键字搜索只有title商品标题一个字段，直接highlightField.getFragments()[0]
                    String highlightName = highlightField.getFragments()[0].toString();
                    goods.setTitle(highlightName);
                }
                goodsList.add(goods);
//
//                List<SearchAttr> attrs = goods.getAttrs();
//
//                for (SearchAttr attr : attrs) {
//                    Long attrId = attr.getAttrId();
//                    String attrName = attr.getAttrName();
//                    set.add(attrId);
//                }
                
            }
            searchResponseVo.setGoodsList(goodsList);
        }


        Aggregations aggregations = result.getAggregations();
        if (null != aggregations) {
            // 解析品牌聚合信息
            ParsedLongTerms tmIdAgg = (ParsedLongTerms) aggregations.get("tmIdAgg");
            List<? extends Terms.Bucket> tmIdAggBuckets = tmIdAgg.getBuckets();

            // 普通循环
//        List<SearchResponseTmVo> searchResponseTmVos = new ArrayList<>();
//        for (Terms.Bucket tmIdAggBucket : tmIdAggBuckets) {
//            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
//            searchResponseTmVos.add(searchResponseTmVo);
//        }
            // 流式循环
            List<SearchResponseTmVo> searchResponseTmVos = tmIdAggBuckets.stream().map(bucket -> {
                SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
                // 第一层聚合元素
                Long keyTmId = (Long) bucket.getKey();
                // 第二层聚合元素
                ParsedStringTerms tmNameAgg = (ParsedStringTerms) bucket.getAggregations().get("tmNameAgg");
                String keyTmName = (String) tmNameAgg.getBuckets().get(0).getKey();
                ParsedStringTerms tmLogoUrlAgg = (ParsedStringTerms) bucket.getAggregations().get("tmLogoUrlAgg");
                String keyTmLogoUrl = (String) tmLogoUrlAgg.getBuckets().get(0).getKey();

                searchResponseTmVo.setTmId(keyTmId);
                searchResponseTmVo.setTmName(keyTmName);
                searchResponseTmVo.setTmLogoUrl(keyTmLogoUrl);
                return searchResponseTmVo;
            }).collect(Collectors.toList());

            searchResponseVo.setTrademarkList(searchResponseTmVos);


            // 解析属性聚合
            ParsedNested attrsAgg = (ParsedNested) aggregations.get("attrsAgg");
            ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrsAgg.getAggregations().get("attrIdAgg");
            List<? extends Terms.Bucket> attrIdAggbuckets = attrIdAgg.getBuckets();

            List<SearchResponseAttrVo> searchResponseAttrVos =  attrIdAggbuckets.stream().map(bucket->{
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                // 第二层聚合元素
                Long keyAttrId = (Long)bucket.getKey();
                // 第三层聚合元素
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)bucket.getAggregations().get("attrNameAgg");
                String keyAttrName = attrNameAgg.getBuckets().get(0).getKeyAsString();

                ParsedStringTerms attrValueAgg = (ParsedStringTerms)bucket.getAggregations().get("attrValueAgg");
                List<String> attrValues = attrValueAgg.getBuckets().stream().map(valueBucket->{
                    String keyValueName = valueBucket.getKeyAsString();
                    return keyValueName;
                }).collect(Collectors.toList());

                searchResponseAttrVo.setAttrValueList(attrValues);
                searchResponseAttrVo.setAttrName(keyAttrName);
                searchResponseAttrVo.setAttrId(keyAttrId);

                return searchResponseAttrVo;
            }).collect(Collectors.toList());

            searchResponseVo.setAttrsList(searchResponseAttrVos);

        }

        return searchResponseVo;
    }

    private SearchSourceBuilder getSearchQueryDsl(SearchParam searchParam) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        Long category3Id = searchParam.getCategory3Id();

        String keyword = searchParam.getKeyword();

        String trademark = searchParam.getTrademark();

        String[] props = searchParam.getProps();

        String order = searchParam.getOrder();

        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        // 查询

        // 三级分类
        if (null != category3Id) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("category3Id", category3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        // 关键字
        if(!StringUtils.isEmpty(keyword)){
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("title",keyword);
            boolQueryBuilder.must(matchQueryBuilder);

            // 高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("title");
            highlightBuilder.preTags("<span style='color:red;font-weight:bolder'>");
            highlightBuilder.postTags("</span>");
            searchSourceBuilder.highlighter(highlightBuilder);

        }

        // 商标,商标参数的格式tmId:tmName
        if(!StringUtils.isEmpty(trademark)){
            String[] split = trademark.split(":");
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("tmId",  split[0]);
            boolQueryBuilder.filter(termQueryBuilder);
        }


        // 属性
        if(null!=props&&props.length>0){
            for (String prop : props) {
                String[] split = prop.split(":");

                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                MatchQueryBuilder matchQueryBuilder1 = new MatchQueryBuilder("attrs.attrValue",attrValue);
                MatchQueryBuilder matchQueryBuilder2 = new MatchQueryBuilder("attrs.attrId",attrId);
                MatchQueryBuilder matchQueryBuilder3 = new MatchQueryBuilder("attrs.attrName",attrName);

                // nested的内存bool
                BoolQueryBuilder boolQueryBuilderInner = new BoolQueryBuilder();
                boolQueryBuilderInner.must(matchQueryBuilder1);
                boolQueryBuilderInner.must(matchQueryBuilder2);
                boolQueryBuilderInner.must(matchQueryBuilder3);

                NestedQueryBuilder nestedQueryBuilder = new NestedQueryBuilder("attrs",boolQueryBuilderInner, ScoreMode.None);

                boolQueryBuilder.must(nestedQueryBuilder);
            }
        }
        searchSourceBuilder.query(boolQueryBuilder);

        // 排序
        if(!StringUtils.isEmpty(order)){
            String[] split = order.split(":");

            String field = split[0];
            String orderBy = split[1];

            if(field.equals("1")){
                field = "hotScore";
            }else if(field.equals("2")) {
                field = "price";
            }

            SortOrder a = orderBy.equals("asc")?SortOrder.ASC: SortOrder.DESC;
            System.out.println(a.toString());
            searchSourceBuilder.sort(field, a);
        }




        // 分页控制
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(60);

        // 聚合商标
        TermsAggregationBuilder tmAgg = AggregationBuilders.terms("tmIdAgg").field("tmId");
        tmAgg.subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"));
        tmAgg.subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        searchSourceBuilder.aggregation(tmAgg);

        // 聚合品牌
        NestedAggregationBuilder attrsAgg = AggregationBuilders.nested("attrsAgg", "attrs");// 第一层
        TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attrIdAgg").field("attrs.attrId");// 第二层
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"));// 第三层
        attrIdAgg.subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"));// 第三层
        attrsAgg.subAggregation(attrIdAgg);// 将第二层放入第一层
        searchSourceBuilder.aggregation(attrsAgg);


        System.out.println(searchSourceBuilder.toString());
        return searchSourceBuilder;
    }
}
