package com.atguigu.gmall.search.pojo;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
public class SearchAttrValue {

    //attrId不需要分词，需要建立索引
    @Field(type = FieldType.Long)
    private Long attrId;
    //attrName不需要分词，需要建立索引
    @Field(type = FieldType.Keyword)
    private String attrName;
    //attrValue不需要分词，需要建立索引
    @Field(type = FieldType.Keyword)
    private String attrValue;

}
