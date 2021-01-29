package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    //ES导入数据所需接口1：分页查询spu接口
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    //ES导入数据所需接口2：遍历spu查询spu下的所有sku
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);

    //ES导入数据所需接口4：根据品牌id查询品牌
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //ES导入数据所需接口5：根据分类id查询分类
    @GetMapping("/pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //ES导入数据所需接口6：根据skuId结合分类id，查询销售类型（type=1）的检索规格参数和值（attrName,attrValue)
    @GetMapping("/pms/skuattrvalue/search/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("skuId")Long skuId
    );

    //ES导入数据所需接口7：根据spuId结合分类id，查询销售类型（type=0）的检索规格参数和值（attrName,attrValue）
    @GetMapping("/pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesByCidAndSpuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId
    );

}
