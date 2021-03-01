package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.mapper.AttrMapper;
import com.atguigu.gmall.pms.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.pms.mapper.SpuAttrValueMapper;
import com.atguigu.gmall.pms.vo.GroupVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.AttrGroupMapper;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import org.springframework.util.CollectionUtils;

@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupMapper, AttrGroupEntity> implements AttrGroupService {

    @Autowired
    private AttrMapper attrMapper;

    @Autowired
    private SpuAttrValueMapper spuAttrValueMapper;

    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<AttrGroupEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public List<AttrGroupEntity> queryAttrGroupsByCatId(Long catId) {
        //1. 根据分组id查询分组
        List<AttrGroupEntity> attrGroupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", catId));
        //2. 遍历分组的id查询组下的规格参数
        if(CollectionUtils.isEmpty(attrGroupEntities)){
            return null;
        }
        attrGroupEntities.forEach(attrGroupEntity -> {
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>()
                    .eq("group_id", attrGroupEntity.getId())
                    .eq("type",1));
            attrGroupEntity.setAttrEntities(attrEntities);
        });
        return attrGroupEntities;
    }

    @Override
    public List<GroupVo> queryGroupWithAttrValuesBy(Long cid, Long spuId, Long skuId) {

        //1. 根据分类id查询出所有的分组信息
        List<AttrGroupEntity> groupEntities = this.list(new QueryWrapper<AttrGroupEntity>().eq("category_id", cid));

        if(CollectionUtils.isEmpty(groupEntities)){
            return null;
        }

        groupEntities.stream().map(attrGroupEntity -> {
            GroupVo groupVo = new GroupVo();

            //2. 获取每个分组下的规格参数列表 --> attrIds
            List<AttrEntity> attrEntities = attrMapper.selectList(new QueryWrapper<AttrEntity>().eq("group_id", attrGroupEntity.getId()));
            if(!CollectionUtils.isEmpty(attrEntities)){

                //获取attrId
                List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

                //3. 查询基本的规格参数和值
                //查询基本的规格参数和值

                //查询销售的规格参数和值
            }


        }).collect(Collectors.toList());


    }

}