package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface SkuService {

    IPage<SkuInfo> skuList(Long page, Long limit);

    void saveSkuInfo(SkuInfo skuInfo);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    SkuInfo getSkuInfoById(Long skuId);

    BigDecimal getSkuPriceById(Long skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long spuId, Long skuId);

    List<Map<String, Object>> getSaleAttrValuesBySpu(Long spuId);

    List<SearchAttr> getSearchAttr(Long skuId);
}
