package com.atguigu.gmall.item.vo;

import lombok.Data;

import java.util.List;

@Data
public class GroupVo {

    private Long id;
    private String name;
    private List<AttrValueVo> attrValue;

}
