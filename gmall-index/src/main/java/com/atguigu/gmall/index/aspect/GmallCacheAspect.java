package com.atguigu.gmall.index.aspect;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.config.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class GmallCacheAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RBloomFilter rBloomFilter;

    @Around("@annotation(com.atguigu.gmall.index.config.GmallCache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        //0. 准备工作，获取缓存的key

        //获取方法签名signature，signature是获取被加强方法N多信息的入口
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        //通过signature获取方法对象
        Method method = signature.getMethod();
        //通过signature获取方法的返回值类型
        Class returnType = signature.getReturnType();
        //获取方法上的注解对象
        GmallCache gmallCache = method.getAnnotation(GmallCache.class);
        //获取注解对象中的前缀
        String prefix = gmallCache.prefix();

        //获取方法参数，返回的是一个数组，而数组的toString()方法返回的是地址，需要处理
        List<Object> args = Arrays.asList(joinPoint.getArgs());

        //组装缓存的key
        String key =  prefix + args;

        //0.5 先用布隆过滤器过滤掉一定错的请求，因为布隆过滤器判断出来不存在的数据，则一定不存在
        if (!rBloomFilter.contains(key)) {
            return null;
        }

        //1. 先查询缓存，如果缓存命中，直接返回
        String json = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseObject(json, returnType);
        }

        //2. 为了防止缓存击穿，添加分布式锁
        String lock = gmallCache.lock();//通过被加强方法的注解，得到注解中的lock属性
        RLock fairLock = redissonClient.getFairLock(lock + args);
        fairLock.lock();

        //3. 再查询缓存，如果可以命中，直接返回（再重复第一步的逻辑，提高在第一个线程拿到锁操作完成后，其他线程不用操作直接返回的效率）
        String json2 = redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(json2)){
            return JSON.parseObject(json2, returnType);
        }

        //4. 执行目标方法，以要调用joinpoint的proceed方法执行目标方法
        Object result = joinPoint.proceed(joinPoint.getArgs());

        //5. 把数据放入缓存
        //为了防止缓存雪崩，给锁的过期时间添加随机值
        if(result != null){
            int timeout = gmallCache.timeout() + new Random().nextInt(gmallCache.random());
            redisTemplate.opsForValue().set(key,JSON.toJSONString(result), timeout, TimeUnit.MINUTES);
        }else {
            //为了防止缓存穿透，返回结果集即使是null，也添加到缓存，设置过期时间为5分钟
            redisTemplate.opsForValue().set(key,JSON.toJSONString(result), 5, TimeUnit.MINUTES);
        }

        //6. 解锁
        fairLock.unlock();

        return result;

    }

}
