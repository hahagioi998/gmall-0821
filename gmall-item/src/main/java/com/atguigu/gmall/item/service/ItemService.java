package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
public class ItemService {

    @Autowired
    private GmallPmsClient gmallPmsClient;

    @Autowired
    private GmallSmsClient gmallSmsClient;

    @Autowired
    private GmallWmsClient gmallWmsClient;

    public ItemVo loadData(Long skuId) {
        ItemVo itemVo = new ItemVo();
        //获取sku的相关信息
        itemVo.setSkuId(skuId);
        itemVo.setTitle(null);
        itemVo.setTitle(null);
        itemVo.setDefaultImage(null);
        itemVo.setPrice(null);
        itemVo.setWeight(null);


        return itemVo;
    }
}
