package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SkuSaleVo {

    private Long skuId;

    //积分优惠信息字段 from com.atguigu.gmall.sms.entity.SkuBoundsEntity
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    //满减优惠信息字段 from com.atguigu.gmall.sms.entity.SkuFullReductionEntity
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer addOther;

    //打折优惠信息字段 from com.atguigu.gmall.sms.entity.SkuLadderEntity
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;

}
