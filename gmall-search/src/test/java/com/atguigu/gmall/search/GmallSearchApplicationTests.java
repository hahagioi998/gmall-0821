package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Test
    void contextLoads() {
        //只需要第一次的时候创建索引库，之后都不用了，所以写在test类中运行一次即可，one-off
        //两步：创建索引库restTemplate.createIndex；创建映射关系restTemplate.putMapping
        restTemplate.createIndex(Goods.class);
        restTemplate.putMapping(Goods.class);

        Integer pageNum = 1;
        Integer pageSize = 100;

        do {
            //分页查询sku
            PageParamVo pageParamVo = new PageParamVo(pageNum, pageSize, null);
            ResponseVo<List<SpuEntity>> listResponseVo = gmallPmsClient.querySpuByPageJson(pageParamVo);
            List<SpuEntity> spuEntities = listResponseVo.getData();

            if(CollectionUtils.isEmpty(spuEntities)){
                break;
            }
            //遍历spu，查询每个spu下的所有sku
            spuEntities.forEach(spuEntity -> {
                ResponseVo<List<SkuEntity>> skuResponseVo = gmallPmsClient.querySkusBySpuId(spuEntity.getId());
                List<SkuEntity> skuEntities = skuResponseVo.getData();
                if(!CollectionUtils.isEmpty(skuEntities)){
                    List<Goods> goodsList = skuEntities.stream().map(skuEntity -> {
                        Goods goods = new Goods();
                        //设置创建时间
                        goods.setCreateTime(spuEntity.getCreateTime());
                        //设置sku相关信息
                        goods.setSkuId(skuEntity.getId());
                        goods.setTitle(skuEntity.getTitle());
                        goods.setPrice(skuEntity.getPrice().doubleValue());
                        goods.setDefaultImage(skuEntity.getDefaultImage());
                        goods.setSubTitle(skuEntity.getSubtitle());
                        //获取库存信息：销量和是否有货
                        ResponseVo<List<WareSkuEntity>> wareResponseVo = gmallWmsClient.queryWareSkuEntitiesBySkuId(skuEntity.getId());
                        List<WareSkuEntity> wareSkuEntities = wareResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            //数据库表中有不同仓库的同一个sku的inventories，所以需要求和计算销量
                            goods.setSales(wareSkuEntities.stream()
                                    .map(WareSkuEntity::getSales)
                                    .reduce((a, b) -> a + b).get());
                            goods.setStore(wareSkuEntities.stream()
                                    .anyMatch(wareSkuEntity -> wareSkuEntity.getStock() - wareSkuEntity.getStockLocked() > 0
                                    ));
                        }

                        //品牌
                        ResponseVo<BrandEntity> brandEntityResponseVo = gmallPmsClient.queryBrandById(skuEntity.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(brandEntity.getId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }

                        //分类
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = gmallPmsClient.queryCategoryById(skuEntity.getCategoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(categoryEntity.getId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        //检索参数
                        List<SearchAttrValue> attrValues = new ArrayList<>();
                        ResponseVo<List<SkuAttrValueEntity>> saleAttrValueResponseVo = gmallPmsClient.querySearchAttrValuesByCidAndSkuId(skuEntity.getCategoryId(), skuEntity.getId());
                        List<SkuAttrValueEntity> skuAttrValueEntities = saleAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                            List<SearchAttrValue> collect = skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList());
                            attrValues.addAll(collect);
                        }
                        ResponseVo<List<SpuAttrValueEntity>> baseAttrValueResponseVo = gmallPmsClient.querySearchAttrValuesByCidAndSpuId(spuEntity.getCategoryId(), spuEntity.getId());
                        List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrValueResponseVo.getData();
                        if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                            List<SearchAttrValue> collect = spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                SearchAttrValue searchAttrValue = new SearchAttrValue();
                                BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                                return searchAttrValue;
                            }).collect(Collectors.toList());
                            attrValues.addAll(collect);
                        }

                        goods.setSearchAttrs(attrValues);
                        return goods;
                    }).collect(Collectors.toList());
                    goodsRepository.saveAll(goodsList);
                }

            });

            pageSize = spuEntities.size();
            pageNum++;
        }while (pageSize == 100);

    }

}
