package com.atguigu.gmall.pms.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class SaleAttrValueVo {

    private Long attrId;
    private String attrName;//内存大小
    private Set<String> attrValues;//4g 6g 8g 12g

}
