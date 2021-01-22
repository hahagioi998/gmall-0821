package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.vo.SkuVo;
import org.apache.commons.lang3.StringUtils;
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

import com.atguigu.gmall.pms.mapper.SkuImagesMapper;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.service.SkuImagesService;
import org.springframework.util.CollectionUtils;


@Service("skuImagesService")
public class SkuImagesServiceImpl extends ServiceImpl<SkuImagesMapper, SkuImagesEntity> implements SkuImagesService {

    @Autowired
    private SkuImagesService skuImagesService;

    public void bigSave2PmsSkuImages(SkuVo sku, List<String> images, Long skuId) {
        if(!CollectionUtils.isEmpty(images)){
            skuImagesService.saveBatch(images.stream().map(image->{
                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                skuImagesEntity.setUrl(image);
                skuImagesEntity.setSkuId(skuId);
                skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(),image)?1:0);
                return skuImagesEntity;
            }).collect(Collectors.toList()));
        }
    }

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuImagesEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuImagesEntity>()
        );

        return new PageResultVo(page);
    }

}