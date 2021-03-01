package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.vo.SaleAttrValueVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {

    //商品详情页所需接口8：根据sku中的spuId查询spu下所有的销售属性
    @GetMapping("pms/skuattrvalue/spu/{spuId}")
    public ResponseVo<List<SaleAttrValueVo>> querySaleAttrsBySpuId(@PathVariable("spuId")Long spuId);

    //商品详情页所需接口4：根据spuId查询spu
    @GetMapping("pms/spu/{id}")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);

    //商品详情页所需接口10：根据spuId查询spu的描述信息
    @GetMapping("{spuId}")
    public ResponseVo<SpuDescEntity> querySpuDescById(@PathVariable("spuId") Long spuId);

    //ES导入数据所需接口1：分页查询spu接口
    @PostMapping("pms/spu/json")
    public ResponseVo<List<SpuEntity>> querySpuByPageJson(@RequestBody PageParamVo paramVo);

    //ES导入数据所需接口2：遍历spu查询spu下的所有sku
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);

    //商品详情页所需接口1：根据skuId查询sku
    @GetMapping("pms/sku/{id}")
    public ResponseVo<SkuEntity> querySkuById(@PathVariable("id") Long id);

    //商品详情页所需接口5：根据spuId查询sku的图片信息
    @GetMapping("pms/skuimages/sku/{skuId}")
    public ResponseVo<List<SkuImagesEntity>> queryImagesBySkuId(@PathVariable("skuId")Long skuId);

    //ES导入数据所需接口4：根据品牌id查询品牌
    //商品详情页所需接口3：根据品牌id查询品牌
    @GetMapping("pms/brand/{id}")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);

    //ES导入数据所需接口5：根据分类id查询分类
    @GetMapping("pms/category/{id}")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);

    //商品详情页所需接口2：根据三级分类的id查询一二三级分类
    @GetMapping("pms/category/all/{cid}")
    public ResponseVo<List<CategoryEntity>> query123CategoriesByCid3(@PathVariable("cid")Long cid);

    //首页工程index-service的查询一级分类的接口，可以把parentId传为0即可，表示自己就是一级分类
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo<List<CategoryEntity>> queryCategoriesByPid(@PathVariable("parentId")Long pid);

    @GetMapping("pms/category/parent/withsubs/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLvl2CatesWithSubsByPid(@PathVariable("pid")Long pid);

    //ES导入数据所需接口6：根据skuId结合分类id，查询销售类型（type=1）的检索规格参数和值（attrName,attrValue)
    @GetMapping("pms/skuattrvalue/search/{cid}")
    public ResponseVo<List<SkuAttrValueEntity>> querySearchAttrValuesByCidAndSkuId(
            @PathVariable("cid")Long cid,
            @RequestParam("skuId")Long skuId
    );

    //商品详情页所需接口10：根据skuId查询spu下所有销售属性组合与skuId的映射关系
    @GetMapping("pms/skuattrvalue/sku/{skuId}")
    public ResponseVo<List<SkuAttrValueEntity>> querySaleAttrValuesBySkuId(@PathVariable("skuId")Long skuId);

    //ES导入数据所需接口7：根据spuId结合分类id，查询销售类型（type=0）的检索规格参数和值（attrName,attrValue）
    @GetMapping("pms/spuattrvalue/search/{cid}")
    public ResponseVo<List<SpuAttrValueEntity>> querySearchAttrValuesByCidAndSpuId(
            @PathVariable("cid")Long cid,
            @RequestParam("spuId")Long spuId
    );

}
