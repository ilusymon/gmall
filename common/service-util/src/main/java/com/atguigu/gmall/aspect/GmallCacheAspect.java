package com.atguigu.gmall.aspect;


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    RedisTemplate redisTemplate;

    @Around("@annotation(com.atguigu.gmall.aspect.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point) {
        Object proceed = null;

        // 前面
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        String name = method.getName();
        Class<?> returnType = method.getReturnType();

        GmallCache annotation = methodSignature.getMethod().getAnnotation(GmallCache.class);

        // 拼接缓存key
        Object[] args = point.getArgs();
        String argKey = "";
        if (null != args && args.length > 0) {
            for (Object arg : args) {
                // 判断arg是否是自定义的引用类型
                boolean b = ifUserObject(arg);
                if (b) {
                    argKey = argKey + ":" + arg.getClass().getTypeName();
                } else {

                    argKey = argKey + ":" + arg;
                }
            }
        }

        // name.equalsIgnoreCase("sku")
        String type = "str";
        if (name.toLowerCase().contains("sku")) {
            String keyPrefix = annotation.skuCache();
            type = annotation.skuType();
            name = keyPrefix;
        }

        // 中间
        // 获取缓存值
        String commonKey = "GmallCache:" + name + argKey;

        proceed = redisTemplate.opsForValue().get(commonKey);

        if (null == proceed) {

            String lockTag = UUID.randomUUID().toString();
            Boolean OK = redisTemplate.opsForValue().setIfAbsent(commonKey+":lock", lockTag,3, TimeUnit.SECONDS);

            if(OK){
                try {
                    proceed = point.proceed();// 查db
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
                // 同步缓存
                if (null != proceed) {
                    if(type.equals("str")){
                        redisTemplate.opsForValue().set(commonKey, proceed);
                    }else if(type.equals("list")){
                        // redisTemplate.opsForList();
                        redisTemplate.opsForValue().set(commonKey, proceed);
                    }else if (type.equals("hash")){
                        // redisTemplate.opsForHash();
                        redisTemplate.opsForValue().set(commonKey, proceed);
                    }
                }else {
                    redisTemplate.opsForValue().set(commonKey, null,1,TimeUnit.MINUTES);
                }


                // 释放分布式锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";// 脚本查询是否存在存在则删除否则返回0
                // 设置lua脚本返回的数据类型
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                // 设置lua脚本返回类型为Long
                redisScript.setResultType(Long.class);
                redisScript.setScriptText(script);
                Long execute = (Long)redisTemplate.execute(redisScript, Arrays.asList(commonKey+":lock"), lockTag);// 执行脚本

            }else {
                // 自旋
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                proceed = redisTemplate.opsForValue().get(commonKey);
                return proceed;
            }
        }

        // 后面
        return proceed;
    }


    public boolean ifUserObject(Object type) {

        boolean b = false;
        String typeName = type.getClass().getTypeName();

        if (!typeName.toLowerCase().contains("long") && !typeName.toLowerCase().contains("integer") && !typeName.toLowerCase().contains("bigdecimal") && !typeName.toLowerCase().contains("string")) {
            b = true;
        }

        return b;
    }

}
