package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.vo.GroupVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class ItemVo {

    //商品详情页第一部分：面包屑

    //breadcrumb：分类
    private List<CategoryEntity> categories;

    //breadcrumb：品牌
    private Long brandId;
    private String brandName;

    //breadcrumb：spu
    private String spuId;
    private String spuName;

    //商品详情页第二部分：sku详细信息

    //sku信息
    private Long skuId;
    private String title;
    private String subTitle;
    private String defaultImage;
    private BigDecimal price;
    private Integer weight;

    //左侧图片
    private List<SkuImagesEntity> images;

    //营销信息，打折啊，满减啊
    private List<ItemSaleVo> sales;

    //有货无货
    private Boolean store = false;

    //跟当前sku相同的spu下的所有sku的销售属性列表，比如具体的选择手机型号的销售信息，内存大小啊，存储大小啊
    //形如：[{attrId:4, attrName:"颜色", attrValues:["黑色","白色"]},
    //          {attrId:5, attrName:"内存大小", attrValues:["4g","8g"]}]
    private List<SaleAttrValueVo> saleAttrs;

    //{4:"黑色", 5:"8g"}
    //当前sku的销售参数
    private Map<Long, Object> saleAttr;

    //销售属性组合和skuId的映射关系
    //比如用户勾选了黑色，4g，则对应skuId是1
    private String skuJsons;

    //商品详情页第三部分：商品的海报信息
    private List<String> spuImages;

    //规格参数分组列表
    private List<GroupVo> groups;

}
