package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {

    @Autowired
    ListFeignClient listFeignClient;

    @RequestMapping({"index.html","/"})
    public String index(Model model){

        List<JSONObject> jsonObjectList = listFeignClient.getBaseCategoryList();

        model.addAttribute("list",jsonObjectList);
        return "index/index";
    }

    @RequestMapping({"list.html","search.html"})
    public String list(Model model,SearchParam searchParam){

        // 查询页面的渲染数据
        SearchResponseVo searchResponseVo = listFeignClient.list(searchParam);

        model.addAttribute("goodsList",searchResponseVo.getGoodsList());
        model.addAttribute("trademarkList",searchResponseVo.getTrademarkList());
        model.addAttribute("attrsList",searchResponseVo.getAttrsList());

        // searchParam.setKeyword("sdkfsfdlfsldfjslfjf");

        String urlParam = getUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);

        // 品牌面包屑
        if(!StringUtils.isEmpty(searchParam.getTrademark())){
            model.addAttribute("trademarkParam", searchParam.getTrademark().split(":")[1]);
        }

        // 属性面包屑
        if(null!=searchParam.getProps()&&searchParam.getProps().length>0){
            List<SearchAttr> searchAttrs = new ArrayList<>();
            for (String prop : searchParam.getProps()) {
                String[] split = prop.split(":");
                Long attrId = Long.parseLong(split[0]);
                String attrValue = split[1];
                String attrName = split[2];
                SearchAttr searchAttr = new SearchAttr();

                searchAttr.setAttrId(attrId);
                searchAttr.setAttrName(attrName);
                searchAttr.setAttrValue(attrValue);
                searchAttrs.add(searchAttr);
            }
            model.addAttribute("propsParamList", searchAttrs);
        }

        // 排序页面参数
        if(null!=searchParam.getOrder()&&!searchParam.getOrder().equals("")){
            Map<String,String> orderMap = new HashMap<>();
            orderMap.put("type",""+searchParam.getOrder().split(":")[0]);
            orderMap.put("sort",""+searchParam.getOrder().split(":")[1]);
            model.addAttribute("orderMap",orderMap);
        }


        return "list/index";
    }

    private String getUrlParam(SearchParam searchParam) {

        String urlParam = "list.html?";

        // SearchParam searchParam 参数中包含哪些数据，就说明当前请求的url包含哪些参数
        Long category3Id = searchParam.getCategory3Id();

        String keyword = searchParam.getKeyword();

        String trademark = searchParam.getTrademark();

        String[] props = searchParam.getProps();

        String order = searchParam.getOrder();

        // 三级分类
        if (null != category3Id) {
            urlParam = urlParam + "category3Id=" + category3Id;
        }

        // 关键字
        if(!StringUtils.isEmpty(keyword)){
            urlParam = urlParam + "keyword=" + keyword;
        }

        // 商标,商标参数的格式tmId:tmName
        if(!StringUtils.isEmpty(trademark)){
            urlParam = urlParam + "&trademark=" + trademark;
        }

        // 属性参数的拼接 通过？aList=1&aList=2&aList=3
        if(null!=props&&props.length>0){
            for (String prop : props) {
                urlParam = urlParam + "&props=" + prop;
            }

        }



        return urlParam;
    }

}
