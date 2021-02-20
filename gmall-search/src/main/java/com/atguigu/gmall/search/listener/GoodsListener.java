package com.atguigu.gmall.search.listener;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttrValue;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoodsListener {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "SEARCH_INSERT_QUEUE", durable = "true"),
            exchange = @Exchange(value = "PMS_ITEM_EXCHANGE",
                    ignoreDeclarationExceptions = "true",
                    type = ExchangeTypes.TOPIC),
            key = {"item.insert"}
    ))

    public void listener(Long spuId, Channel channel, Message message) throws IOException {

        if(spuId == null){
            //如果是一个空的消息，则直接消费掉
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false); //false不批量确认
            return;//后续不再执行
        }

        //查询spu，从而获取到spuEntity，之后查询createTime需要用，所以要先查出来
        ResponseVo<SpuEntity> spuEntityResponseVo = gmallPmsClient.querySpuById(spuId);
        SpuEntity spuEntity = spuEntityResponseVo.getData();
        if(spuEntity == null){
            //如果未空，则也是一个垃圾消息，直接确认消费掉
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            return;//后续不再执行
        }

        //直接从测试用例中导入

        ResponseVo<List<SkuEntity>> skuResponseVo = gmallPmsClient.querySkusBySpuId(spuId);
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

        //操作完毕后，消费者最终确认消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

    }

}
