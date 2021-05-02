package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuPosterMapper spuPosterMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;

    @Override
    public IPage<SpuInfo> spuList(Long page, Long limit, Long category3Id) {
        Page<SpuInfo> pageParam = new Page<>();

        pageParam.setSize(limit);
        pageParam.setCurrent(page);

        QueryWrapper<SpuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);

        IPage<SpuInfo> iPage = spuInfoMapper.selectPage(pageParam, queryWrapper);

        return iPage;
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        // 添加spu，生成主键id
        spuInfoMapper.insert(spuInfo);

        Long spu_id = spuInfo.getId();

        // 添加图片集合，海报姐还
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(null!=spuImageList&&spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spu_id);
                spuImageMapper.insert(spuImage);
            }
        }


        // 添加销售属性集合
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(null!=spuSaleAttrList&&spuSaleAttrList.size()>0){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spu_id);
                spuSaleAttrMapper.insert(spuSaleAttr);


                // 添加销售属性值集合
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();

                if(null!=spuSaleAttrValueList&&spuSaleAttrValueList.size()>0){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spu_id);
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }

            }
        }


    }

    @Override
    public List<SpuImage> spuImageList(Long spuId) {

        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("spu_id",spuId);

        List<SpuImage> spuImages = spuImageMapper.selectList(queryWrapper);
        return spuImages;
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        QueryWrapper<SpuSaleAttr> queryWrapper = new QueryWrapper<>();

        queryWrapper.eq("spu_id",spuId);

        List<SpuSaleAttr> spuSaleAttrs = spuSaleAttrMapper.selectList(queryWrapper);

        for (SpuSaleAttr spuSaleAttr : spuSaleAttrs) {

            QueryWrapper<SpuSaleAttrValue> queryWrapperValue = new QueryWrapper<>();

            queryWrapperValue.eq("spu_id",spuId);
            queryWrapperValue.eq("base_sale_attr_id",spuSaleAttr.getBaseSaleAttrId());

            List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(queryWrapperValue);

            spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
        }

        return spuSaleAttrs;
    }
}
