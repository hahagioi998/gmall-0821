package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;
import java.util.List;

@Data
//索引库名=goods，索引表=info，分片=3，副本=2
@Document(indexName = "goods", type = "info", shards = 3, replicas = 2)
public class Goods {

    //1. 商品搜索页展示--最下面的商品结果列表，显示所需要的字段
    //纯粹用来显示的字段，不需要创建索引；需要通过该字段来操作（如搜索，排序，过滤，聚合等），需要创建索引

    //商品列表所需的字段
    @Id
    private Long skuId;
    //defaultImage不需要分词，不需要建立索引
    @Field(type = FieldType.Keyword, index = false)
    private String defaultImage;
    //price不需要分词，需要建立索引
    @Field(type = FieldType.Double)//易错：虽然不用分词，但不是FieldType.Keyword，而是FieldType.Double
    private Double price;
    //title需要分词，需要建立索引
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;
    //subTitle不需要分词，不需要建立索引
    @Field(type = FieldType.Keyword, index = false)
    private String subTitle;

    //2. 商品搜索页面展示--中间的各种按条件进行排序，或者进行过滤的，所需要的字段
    //用来排序或过滤的字段，都需要建立索引

    //2.1 排序所需字段
    //sales不需要分词，需要建立索引
    @Field(type = FieldType.Long)//易错：虽然不用分词，但不是FieldType.Keyword，而是FieldType.Integer
    private Long sales = 0L;//销量
    //createTime不需要分词，需要建立索引（因为是用来排序的字段！）
    @Field(type = FieldType.Date)
    private Date createTime;//新品创建时间

    //2.2 过滤的库存字段
    //store不需要分词，需要建立索引
    @Field(type = FieldType.Boolean)
    private Boolean store = false;//库存

    //3. 商品搜索页面展示--最上面的各种聚合信息字段
    //用来聚合的字段，都需要建立索引

    //3.1 品牌聚合所需字段
    //brandId不需要分词，需要建立索引
    @Field(type = FieldType.Long)
    private Long brandId;
    //brandName不需要分词，需要建立索引
    @Field(type = FieldType.Keyword)
    private String brandName;
    //logo不需要分词，需要建立索引
    @Field(type = FieldType.Keyword)
    private String logo;

    //3.2 分类聚合所需字段
    //categoryId不需要分词，需要建立索引
    @Field(type = FieldType.Long)
    private Long categoryId;
    //categoryName不需要分词，需要建立索引
    @Field(type = FieldType.Keyword)
    private String categoryName;

    //3.3 规格参数聚合所需字段
    //searchAttrs包含了attrId，attrName，attrValue，不需要分词，需要建立索引
    //用FieldType.Nested表示嵌套类型的，然后去searchAttrValue这个pojo中去添加相应注解
    @Field(type = FieldType.Nested)
    private List<SearchAttrValue> searchAttrs;

}
