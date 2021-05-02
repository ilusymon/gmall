package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.AttrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttrServiceImpl implements AttrService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> attrInfoList(Long category3Id) {

        QueryWrapper<BaseAttrInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_level",3);

        queryWrapper.eq("category_id",category3Id);

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectList(queryWrapper);

        if(null!=baseAttrInfos&&baseAttrInfos.size()>0){
            for (BaseAttrInfo baseAttrInfo : baseAttrInfos) {
                QueryWrapper<BaseAttrValue> queryWrapper1 = new QueryWrapper<>();
                queryWrapper1.eq("attr_id",baseAttrInfo.getId());
                List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(queryWrapper1);
                baseAttrInfo.setAttrValueList(baseAttrValues);
            }
        }

        return baseAttrInfos;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        Long id = baseAttrInfo.getId();

        if(null!=id&&id!=-1){
            // 修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
            // 删除+新增=修改
            QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("attr_id",id);
            baseAttrValueMapper.delete(queryWrapper);
        }else {
            // 保存
            baseAttrInfoMapper.insert(baseAttrInfo);
            Long attr_id = baseAttrInfo.getId();
            id = attr_id;
        }
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if(null!=attrValueList&&attrValueList.size()>0){
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setAttrId(id);
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attr_id) {

        QueryWrapper<BaseAttrValue> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_id",attr_id);
        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(queryWrapper);

        return baseAttrValues;
    }

}
