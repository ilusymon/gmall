package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.TrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TrademarkServiceImpl implements TrademarkService {

    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public IPage<BaseTrademark> baseTrademark(Long page, Long limit) {

        Page<BaseTrademark> pageParam = new Page<>();

        pageParam.setSize(limit);
        pageParam.setCurrent(page);

        IPage<BaseTrademark> iPage = baseTrademarkMapper.selectPage(pageParam, null);

        return iPage;
    }

    @Override
    public List<BaseTrademark> getTrademarkList() {


        return baseTrademarkMapper.selectList(null);
    }

    @Override
    public BaseTrademark getTrademarkById(Long tmId) {

        BaseTrademark baseTrademark = baseTrademarkMapper.selectById(tmId);

        return baseTrademark;
    }
}
