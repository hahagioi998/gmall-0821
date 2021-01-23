package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SkuSaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;

@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private SpuDescServiceImpl spuDescService;

    @Autowired
    private SpuAttrValueServiceImpl spuAttrValueService;

    @Autowired
    private SkuServiceImpl skuService;

    @Autowired
    private SkuImagesServiceImpl skuImagesService;

    @Autowired
    private SkuAttrValueServiceImpl skuAttrValueService;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuListBySearchConditionsAndCategoryId(Long categoryId, PageParamVo pageParamVo) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        //如果categoryId不指定，则查询所有分类下的spu
        if(categoryId != 0){
            wrapper.eq("category_id",categoryId);
        }
        //用户搜索条件
        String key = pageParamVo.getKey();
        if(StringUtils.isBlank(key)){
            //函数式接口有四种：这里是消费型接口，里面用lambda表达式写出来消费一个接口即可
            //消费型接口有参数，没有返回值
            //供给型接口没参数，有返回值
            //功能型接口有参数，有返回值
            //判断型接口有参数，返回值是true/false
            wrapper.and(t->t.eq("id",key).or().like("name",key));
        }
        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );
        return new PageResultVo(page);
    }

    @Override
    @GlobalTransactional
    public void bigSave(SpuVo spu) {

        //1. 先保存spu相关信息

        //1.1 保存pms_spu
        Long spuId = bigSave2PmsSpu(spu);

        //1.2 保存pms_spu_desc
        spuDescService.bigSave2PmsSpuDesc(spu, spuId);

        //1.3 保存pms_spu_attr_value
        spuAttrValueService.bigSave2PmsSpuAttrValue(spu, spuId);

        //2. 再保存sku相关信息

        List<SkuVo> skus = spu.getSkus();
        if(CollectionUtils.isEmpty(skus)){
            return;
        }

        skus.forEach(sku -> {
            //2.1 保存pms_sku
            List<String> images = skuService.bigSave2PmsSku(spu, spuId, sku);
            Long skuId = sku.getId();

            //2.2 保存pms_sku_images
            skuImagesService.bigSave2PmsSkuImages(sku, images, skuId);

            //2.3 保存pms_sku_attr_value
            skuAttrValueService.bigSave2PmsSkuAttrValue(sku, skuId);

            //3. 最后保存营销信息
            bigSavePms2SmsFeign(sku, skuId);

            //测试seata是否搭建成功：
//            int c = 1/0;
        });

    }

    public void bigSavePms2SmsFeign(SkuVo sku, Long skuId) {
        SkuSaleVo skuSaleVo = new SkuSaleVo();
        BeanUtils.copyProperties(sku,skuSaleVo);
        skuSaleVo.setSkuId(skuId);
        gmallSmsClient.saveSales(skuSaleVo);
    }

    private Long bigSave2PmsSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        //获取出spu保存后的spu_id，这个经常会用到
        return spu.getId();
    }

}