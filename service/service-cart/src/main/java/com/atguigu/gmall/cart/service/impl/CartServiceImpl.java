package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void addCart(CartInfo cartInfo) {

        SkuInfo skuInfoById = productFeignClient.getSkuInfoById(cartInfo.getSkuId());
        BigDecimal price = skuInfoById.getPrice();
        cartInfo.setCartPrice(price.multiply(new BigDecimal(cartInfo.getSkuNum())));
        cartInfo.setIsChecked(1);
        cartInfo.setSkuPrice(skuInfoById.getPrice());
        cartInfo.setImgUrl(skuInfoById.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfoById.getSkuName());
        // cartInfo.setUserId();

        // 判断是添加操作还是修改操作
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cartInfo.getUserId());
        queryWrapper.eq("sku_id", cartInfo.getSkuId());
        CartInfo ifCart = cartInfoMapper.selectOne(queryWrapper);
        if (null == ifCart) {
            cartInfoMapper.insert(cartInfo);
        } else {
            Integer skuNum = ifCart.getSkuNum();//购物车中已有数量
            Integer skuNumAdd = cartInfo.getSkuNum();//本次新增的数量
            BigDecimal add = new BigDecimal("0");
            add = add.add(new BigDecimal(skuNum)).add(new BigDecimal(skuNumAdd));
            cartInfo.setSkuNum(add.intValue());
            cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(add));
            cartInfoMapper.update(cartInfo, queryWrapper);
        }

        // 同步缓存
        String userId = cartInfo.getUserId();

        QueryWrapper<CartInfo> queryWrapperCache = new QueryWrapper();
        queryWrapperCache.eq("user_id", userId);
        List<CartInfo> cartInfos = cartInfoMapper.selectList(queryWrapperCache);
        if (null != cartInfos && cartInfos.size() > 0) {
            redisTemplate.delete(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX);
            for (CartInfo info : cartInfos) {
                redisTemplate.opsForHash().put(RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX, info.getSkuId() + "", info);
            }
        }

    }

    @Override
    public List<CartInfo> cartList(String userId) {
        List<CartInfo> cartInfos = null;
        // 查询缓存
        cartInfos = (List<CartInfo>) redisTemplate.opsForHash().values("user:" + userId + ":cart");

        if (null != cartInfos && cartInfos.size() > 0) {

        } else {
            QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId);
            cartInfos = cartInfoMapper.selectList(queryWrapper);
            for (CartInfo cartInfo : cartInfos) {
                Long skuId = cartInfo.getSkuId();
                SkuInfo skuInfoById = productFeignClient.getSkuInfoById(skuId);
                cartInfo.setSkuPrice(skuInfoById.getPrice());
            }

            // 同步缓存
            Map<String,Object> map = new HashMap<>();
            for (CartInfo cartInfo : cartInfos) {
                map.put(cartInfo.getSkuId()+"",cartInfo);
            }
            redisTemplate.boundHashOps("user:" + userId + ":cart").putAll(map);
        }


        return cartInfos;
    }

    @Override
    public void checkCart(CartInfo cartInfo) {
        QueryWrapper<CartInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", cartInfo.getUserId());
        queryWrapper.eq("sku_id", cartInfo.getSkuId());

        cartInfoMapper.update(cartInfo, queryWrapper);

        // 同步缓存
        CartInfo cartCache = (CartInfo) redisTemplate.opsForHash().get("user:" + cartInfo.getUserId() + ":cart", cartInfo.getSkuId() + "");
        cartCache.setIsChecked(cartInfo.getIsChecked());
        redisTemplate.opsForHash().put("user:" + cartInfo.getUserId() + ":cart", cartInfo.getSkuId() + "", cartCache);

    }
}
