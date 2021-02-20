package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    public List<CategoryEntity> queryLv1Categories() {
        ResponseVo<List<CategoryEntity>> categoryResponseVo = gmallPmsClient.queryCategoriesByPid(0L);
        return categoryResponseVo.getData();
    }

    public List<CategoryEntity> queryLv2CategoriesWithSubsByPid(Long pid) {

    }
}
