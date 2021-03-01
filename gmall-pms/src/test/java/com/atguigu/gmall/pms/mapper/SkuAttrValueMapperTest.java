package com.atguigu.gmall.pms.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SkuAttrValueMapperTest {

    @Autowired
    private SkuAttrValueMapper attrValueMapper;

    @Test
    void querySaleAttrsMappingSkuId() {
        System.out.println(attrValueMapper.querySaleAttrsMappingSkuId(Arrays.asList(10l, 20l)));
    }
}