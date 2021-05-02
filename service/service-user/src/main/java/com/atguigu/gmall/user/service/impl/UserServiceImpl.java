package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.constant.RedisConst;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.atguigu.gmall.util.MD5;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {


    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    UserAddressMapper userAddressMapper;

    @Override
    public Map<String, Object> login(UserInfo userInfo) {

        QueryWrapper<UserInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("login_name",userInfo.getLoginName());
        String passwd = MD5.encrypt(userInfo.getPasswd());
        queryWrapper.eq("passwd", passwd);
        UserInfo userInfoDb = userInfoMapper.selectOne(queryWrapper);

        if(null!=userInfoDb){
            Map<String,Object> map = new HashMap<>();
            map.put("name",userInfoDb.getName());
            map.put("nickName",userInfoDb.getNickName());

            // 生成token，放入redis
            String token = UUID.randomUUID().toString();

            // 将token放入缓存，设置过期时间
            redisTemplate.opsForValue().set(RedisConst.USER_KEY_PREFIX+token,userInfoDb.getId()+"");

            map.put("token",token);
            return map;
        }else {
            return null;
        }

    }

    @Override
    public Map<String, Object> verify(String token) {

        String userId = (String)redisTemplate.opsForValue().get(RedisConst.USER_KEY_PREFIX + token);


        Map<String, Object> map = new HashMap<>();

        map.put("userId",userId);
        return map;
    }

    @Override
    public List<UserAddress> findUserAddressListByUserId(String userId) {

        QueryWrapper<UserAddress> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id",userId);
        List<UserAddress> userAddresses = userAddressMapper.selectList(queryWrapper);

        return userAddresses;
    }
}
