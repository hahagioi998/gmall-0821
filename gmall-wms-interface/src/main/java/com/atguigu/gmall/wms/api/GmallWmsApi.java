package com.atguigu.gmall.wms.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface GmallWmsApi {

    //ES导入数据所需接口3：根据skuId查询sku的库存信息（是否有货：store & 销量：sales）
    @GetMapping("/wms/waresku//sku/{skuId}")
    public ResponseVo<List<WareSkuEntity>> queryWareSkuEntitiesBySkuId(@PathVariable("skuId")Long skuId);

}
