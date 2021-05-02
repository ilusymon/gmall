package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService{

    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {

        List<BaseCategory1> category1s = baseCategory1Mapper.selectList(null);

        return category1s;
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {

        QueryWrapper<BaseCategory2> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(queryWrapper);

        return baseCategory2s;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("category2_id",category2Id);

        List<BaseCategory3> baseCategory3s = baseCategory3Mapper.selectList(queryWrapper);

        return baseCategory3s;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {

        QueryWrapper<BaseCategoryView> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("category3_id",category3Id);

        BaseCategoryView categoryView = baseCategoryViewMapper.selectOne(queryWrapper);

        return categoryView;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {
        // 封装分类结构
        List<BaseCategoryView> categoryViews = baseCategoryViewMapper.selectList(null);

        // json中应该有三个字段分别是：
        // categoryId
        // categoryName
        // categoryChild
        List<JSONObject> category1List = new ArrayList<>();


        // 第一层一级分类
        Map<Long, List<BaseCategoryView>> c1Map = categoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> c1Entry : c1Map.entrySet()) {

            Long c1Id = c1Entry.getKey();
            String c1Name = c1Entry.getValue().get(0).getCategory1Name();
            JSONObject jsonObjectC1 = new JSONObject();
            jsonObjectC1.put("categoryId",c1Id);
            jsonObjectC1.put("categoryName",c1Name);

            // 第二层二级分类
            List<JSONObject> category2List = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> c2Map = c1Entry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> c2Entry : c2Map.entrySet()) {
                JSONObject jsonObjectC2 = new JSONObject();
                Long c2Id = c2Entry.getKey();
                String c2Name = c2Entry.getValue().get(0).getCategory2Name();
                jsonObjectC2.put("categoryId",c2Id);
                jsonObjectC2.put("categoryName",c2Name);

                // 第三层三级分类
                List<JSONObject> category3List = new ArrayList<>();
                Map<Long, List<BaseCategoryView>> c3Mp = c2Entry.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                for (Map.Entry<Long, List<BaseCategoryView>> c3Entry : c3Mp.entrySet()) {
                    JSONObject jsonObjectC3 = new JSONObject();
                    Long c3Id = c3Entry.getKey();
                    String c3Name = c3Entry.getValue().get(0).getCategory3Name();
                    jsonObjectC3.put("categoryId",c3Id);
                    jsonObjectC3.put("categoryName",c3Name);
                    category3List.add(jsonObjectC3);
                }
                
                jsonObjectC2.put("categoryChild",category3List);
                category2List.add(jsonObjectC2);
            }
            jsonObjectC1.put("categoryChild",category2List);
            category1List.add(jsonObjectC1);
        }

        File file = new File("d:/category1List.js");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(JSON.toJSONString(category1List).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return category1List;
    }
}
