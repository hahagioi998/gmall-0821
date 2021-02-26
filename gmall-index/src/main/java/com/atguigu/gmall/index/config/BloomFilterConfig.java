package com.atguigu.gmall.index.config;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Configuration
public class BloomFilterConfig {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    private static final String KEY_PREFIX = "index:cates:";

    @Bean
    public RBloomFilter rBloomFilter(){

        //初始化布隆过滤器
        RBloomFilter<Object> bloomFilter = redissonClient.getBloomFilter("index:bloom");
        bloomFilter.tryInit(3000,0.01);//二进制数组长度是3000，误判率不超过1%

        //给布隆过滤器添加初始化数据
        ResponseVo<List<CategoryEntity>> listResponseVo = gmallPmsClient.queryCategoriesByPid(0L);
        List<CategoryEntity> categoryEntities = listResponseVo.getData();
        if(!CollectionUtils.isEmpty(categoryEntities)){
            categoryEntities.forEach(categoryEntity -> {
                bloomFilter.add(KEY_PREFIX + "[" + categoryEntity.getId() + "]");
            });
        }

        return bloomFilter;
    }

}
