package com.atguigu.gmall.search.pojo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

@Data
public class SearchResponseVo {

    //品牌列表渲染所需字段
    private List<BrandEntity> brands;

    //分类列表渲染所需字段
    private List<CategoryEntity> categories;

    //规格参数列表渲染所需字段
    private List<SearchResponseAttrVo> filters;

    //分页渲染所需数据
    private Integer pageNum;
    private Integer pageSize;
    private Long total;//总记录数

    //当前页的具体渲染数据
    private List<Goods> goodsList;

}
