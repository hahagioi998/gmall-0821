package com.atguigu.gmall.pms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.SkuEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.baomidou.mybatisplus.core.conditions.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.entity.SkuAttrValueEntity;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import org.springframework.util.CollectionUtils;


@Service("skuAttrValueService")
public class SkuAttrValueServiceImpl extends ServiceImpl<SkuAttrValueMapper, SkuAttrValueEntity> implements SkuAttrValueService {

    @Autowired
    private SkuAttrValueService skuAttrValueService;

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    public void bigSave2PmsSkuAttrValue(SkuVo sku, Long skuId) {
        List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
        if(!CollectionUtils.isEmpty(saleAttrs)){
            saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
            skuAttrValueService.saveBatch(saleAttrs);
        }
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuAttrValueEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuAttrValueEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<SkuAttrValueEntity> querySearchAttrValuesByCidAndSkuId(Long cid, Long skuId) {
        List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>()
                .eq("category_id", cid)
                .eq("search_type", 1));
        if(CollectionUtils.isEmpty(attrEntities)){
            return null;
        }
        //获取检索类型的id参数集合，有的属于spu（type=0）有的属于sku（type=1）
        List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());
        //查询出销售类型的检索规格参数
        List<SkuAttrValueEntity> attrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>()
                .eq("sku_id", skuId)
                .in("attr_id", attrIds));
        return attrValueEntities;
    }

    @Override
    public List<SaleAttrValueVo> querySaleAttrsBySpuId(Long spuId) {
        List<Long> skuIds = getSkuIdsBySpuId(spuId);
        if (skuIds == null) return null;

        //查询sku对应的销售属性
        List<SkuAttrValueEntity> skuAttrValueEntities = this.list(new QueryWrapper<SkuAttrValueEntity>().in("sku_id", skuIds).orderByAsc("attr_id"));
        if(CollectionUtils.isEmpty(skuAttrValueEntities)){
            return null;
        }

        //对集合重新分组
        //Collectors工具类中groupingBy方法，将集合重新整理，按照某个属性进行分组的操作，参数自然是某个属性/字段，用方法引用即可
        //以attrId分组：attrId-key List<SkuAttrValueEntity>-value 这样的数据结构
        Map<Long, List<SkuAttrValueEntity>> map = skuAttrValueEntities.stream().collect(Collectors.groupingBy(SkuAttrValueEntity::getAttrId));

        //把map转换成我们返回值需要的vo对象
        List<SaleAttrValueVo> saleAttrValueVos = new ArrayList<>();
        map.forEach((attrId, attrValueEntities) -> {
            SaleAttrValueVo saleAttrValueVo = new SaleAttrValueVo();
            saleAttrValueVo.setAttrId(attrId);
            saleAttrValueVo.setAttrName(attrValueEntities.get(0).getAttrName());
            Set<String> set = attrValueEntities.stream().map(SkuAttrValueEntity::getAttrValue).collect(Collectors.toSet());
            saleAttrValueVo.setAttrValues(set);
            saleAttrValueVos.add(saleAttrValueVo);
        });
        return saleAttrValueVos;
    }

    private List<Long> getSkuIdsBySpuId(Long spuId) {
        //查询spu下所有的sku
        List<SkuEntity> skuEntities = skuMapper.selectList(new QueryWrapper<SkuEntity>().eq("spu_id", spuId));
        if(CollectionUtils.isEmpty(skuEntities)){
            return null;
        }
        //搜集所有的skuId
        List<Long> skuIds = skuEntities.stream().map(SkuEntity::getId).collect(Collectors.toList());
        return skuIds;
    }

    @Override
    public String querySaleAttrsMappingSkuIdBySpuId(Long spuId) {

        List<Long> skuIds = getSkuIdsBySpuId(spuId);

        List<Map<String, Object>> maps = skuAttrValueMapper.querySaleAttrsMappingSkuId(skuIds);

        Map<String, Long> mappingMap = maps.stream().collect(Collectors.toMap(
                map -> map.get("attr_values").toString(),
                map -> (Long) map.get("sku_id")));

        //序列化
        return JSON.toJSONString(mappingMap);

    }

}