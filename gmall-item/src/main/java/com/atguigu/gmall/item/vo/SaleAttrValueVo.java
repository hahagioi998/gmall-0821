package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;//内存大小
    private List<String> attrValues;//4g 6g 8g 12g

}
