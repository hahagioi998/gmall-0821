package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.config.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.Around;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.security.Key;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    //用于给redis中的key不冲突，且层级显示，第一层是模块名
    private static final String KEY_PREFIX = "index:cates:";

    public List<CategoryEntity> queryLv1Categories() {
        ResponseVo<List<CategoryEntity>> categoryResponseVo = gmallPmsClient.queryCategoriesByPid(0L);
        return categoryResponseVo.getData();
    }

    @GmallCache(prefix = KEY_PREFIX, timeout = 43200, lock = "index:cates:lock")
    public List<CategoryEntity> queryLv2CategoriesWithSubsByPid2(Long pid) {
        //业务代码只有这两行，用AOP抽取了其他实现分布式锁的功能代码
        ResponseVo<List<CategoryEntity>> responseVo = gmallPmsClient.queryLvl2CatesWithSubsByPid(pid);
        return responseVo.getData();
    }

    public List<CategoryEntity> queryLv2CategoriesWithSubsByPid(Long pid) {
        //先查询缓存，如果有直接返回；如果没有查询db，放入redis
        String json = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json)){
            return JSON.parseArray(json, CategoryEntity.class);
        }

        //为了防止缓存击穿，添加分布式锁
        //锁的名字有讲究，因为如果写死了lock，则不管请求什么资源都是这个锁，key只有这一个，效率极低，每个资源应该各有一个锁
        //所以为了避免锁住别人的资源，只锁当前线程要操作的资源，可以加层级，模块名：数据类型：方法参数，redis中也看得清楚
        RLock lock = redissonClient.getLock("index:cates:lock" + pid);
        lock.lock();

        //再次查询缓存，如果存在则不用放入缓存了，因为在请求等待获取锁的过程中可能有其他请求已经成功查询并放入了缓存
        String json2 = redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotBlank(json2)){
            return JSON.parseArray(json2, CategoryEntity.class);
        }

        //再去查询db，放入redis
        ResponseVo<List<CategoryEntity>> responseVo = gmallPmsClient.queryLvl2CatesWithSubsByPid(pid);
        List<CategoryEntity> categoryEntities = responseVo.getData();
        //为了防止缓存穿透，数据即使不存在也缓存/或者用布隆过滤器，更稳
        if(CollectionUtils.isEmpty(categoryEntities)){
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 5, TimeUnit.MINUTES);
        }else {
            //为了防止缓存雪崩，给缓存时间添加随机值
            redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(categoryEntities), 30 + new Random(10).nextInt(), TimeUnit.DAYS);
        }

        return categoryEntities;
    }

    //测试分布式锁，用Redisson实现
    public void testLock(){

        RLock lock = redissonClient.getLock("lock1");
        lock.lock();

        try{
            //获取到锁，执行业务逻辑
            //这里是让key=number的键值对value++，进行并发情况下lock分布式锁的压力测试，可以用ab工具，比如5000次请求并发，是否number结果是5000
            String number = redisTemplate.opsForValue().get("number");
            if(number == null){
                return;
            }
            int num = Integer.parseInt(number);
            redisTemplate.opsForValue().set("number", String.valueOf(++num));
        }finally{
            //释放锁
            lock.unlock();
        }

    }

}
