package com.atguigu.gmall.search.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public SearchResponseVo search(SearchParamVo searchParamVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(searchParamVo));
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //TODO：解析搜索的结果集searchResponse
            SearchResponseVo searchResponseVo = parseResult(searchResponse);
            //分页参数只有在搜索参数中才有，不在hits和aggregations里
            searchResponseVo.setPageNum(searchParamVo.getPageNum());
            searchResponseVo.setPageSize(searchParamVo.getPageSize());
            return searchResponseVo;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SearchResponseVo parseResult(SearchResponse searchResponse){
        //DSL的searchResponse可以看出由两部分组成：hits，aggregations
        SearchResponseVo responseVo = new SearchResponseVo();
        //1. 解析hits：能获取到总记录数，和当前页的记录列表
        SearchHits hits = searchResponse.getHits();
//        在searchParamVo中获取即可
//        responseVo.setPageNum();
//        responseVo.setPageSize();
        responseVo.setTotal(hits.totalHits);

        //当前页的数据
        SearchHit[] hitsHits = hits.getHits();
        List<Goods> goodsList = Stream.of(hitsHits).map(hitsHit->{
            String sourceAsString = hitsHit.getSourceAsString();
            Goods goods = JSON.parseObject(sourceAsString, Goods.class);
            //获取高亮标题，覆盖掉_source中的普通标题
            Map<String, HighlightField> highlightFields = hitsHit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Text[] fragments = title.getFragments();
            String highlightedTitle = fragments[0].string();
            goods.setTitle(highlightedTitle);
            return goods;
        }).collect(Collectors.toList());

        //TODO：具体解析
        responseVo.setGoodsList(goodsList);

        //2. 解析aggregations：获取到品牌列表，分类列表，规格参数聚合列表
        //把聚合结果集以map的形式解析，才够
        Map<String, Aggregation> aggregationMap = searchResponse.getAggregations().asMap();
        //2.1 获取品牌
        //Aggregation接口方法太少，所以强转成ParsedTerms来使用
        ParsedLongTerms brandIdAgg = (ParsedLongTerms)aggregationMap.get("brandIdAgg");
        List<? extends Terms.Bucket> buckets = brandIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(buckets)){
            List<BrandEntity> collect = buckets.stream().map(bucket -> {
                BrandEntity brandEntity = new BrandEntity();
                //外层桶的key就是品牌的id
                brandEntity.setId(bucket.getKeyAsNumber().longValue());
                //brandName在brandNameAgg这个子聚合中，获取到桶中的子聚合
                Map<String, Aggregation> brandSubAggMap = bucket.getAggregations().asMap();
                ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandSubAggMap.get("brandNameAgg");
                //每个品牌名称子聚合中应该有且仅有一个桶
                String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
                brandEntity.setName(brandName);
                //每个品牌logo子聚合中应该有且仅有一个桶
                ParsedStringTerms brandLogoAgg = (ParsedStringTerms) brandSubAggMap.get("brandLogoAgg");
                if(brandLogoAgg!=null){
                    List<? extends Terms.Bucket> logoBuckets = brandLogoAgg.getBuckets();
                    String logo = logoBuckets.get(0).getKeyAsString();
                    brandEntity.setLogo(logo);
                }
                return brandEntity;
            }).collect(Collectors.toList());
            responseVo.setBrands(collect);
        }
        //2.2 获取分类
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms)aggregationMap.get("categoryIdAgg");
        List<? extends Terms.Bucket> buckets1 = categoryIdAgg.getBuckets();
        if(!CollectionUtils.isEmpty(buckets1)){
            //把每个桶转换成每个分类
            List<CategoryEntity> collect = buckets1.stream().map(bucket -> {
                CategoryEntity categoryEntity = new CategoryEntity();
                categoryEntity.setId(bucket.getKeyAsNumber().longValue());
                //获取分类名称子聚合
                ParsedStringTerms categoryNameAgg = (ParsedStringTerms) bucket.getAggregations().get("categoryNameAgg");
                List<? extends Terms.Bucket> nameAggBuckets = categoryNameAgg.getBuckets();
                String categoryName = nameAggBuckets.get(0).getKeyAsString();
                categoryEntity.setName(categoryName);
                return categoryEntity;
            }).collect(Collectors.toList());
            responseVo.setCategories(collect);
        }
        //2.3 获取规格参数
        //获取到规格参数的嵌套集合
        ParsedNested attrAgg = (ParsedNested)aggregationMap.get("attrAgg");
        //获取到嵌套集合中的attrId聚合
        ParsedLongTerms attrIdAgg = (ParsedLongTerms)attrAgg.getAggregations().get("attrIdAgg");
        //获取attrId聚合中的桶集合，获取所有的j检索类型的规格参数
        List<? extends Terms.Bucket> buckets2 = attrIdAgg.getBuckets();
        //有些商品或者有些关键字可能没有检索类型的规格参数，防止空指针异常所以先判空
        if(!CollectionUtils.isEmpty(buckets2)){
            //把attrId中的桶集合转化成我们需要的List<SearchResponseAttrVo>
            List<SearchResponseAttrVo> searchResponseAttrVos =  buckets2.stream().map(bucket->{
                //把每个桶转化成searchResponseAttrVo对象
                SearchResponseAttrVo searchResponseAttrVo = new SearchResponseAttrVo();
                //设置数据
                //桶中的key就是attrId
                searchResponseAttrVo.setAttrId(bucket.getKeyAsNumber().longValue());
                //获取bucket中的两个聚合，所以用map比较好找出谁是谁
                Map<String, Aggregation> subAggMap = bucket.getAggregations().asMap();
                //获取attrName子聚合
                ParsedStringTerms attrNameAgg = (ParsedStringTerms)subAggMap.get("attrNameAgg");
                String attrName = attrNameAgg.getBuckets().get(0).getKeyAsString();
                searchResponseAttrVo.setAttrName(attrName);
                //获取attrValue子聚合
                ParsedStringTerms attrValueAgg = (ParsedStringTerms)subAggMap.get("attrValueAgg");
                List<? extends Terms.Bucket> buckets3 = attrValueAgg.getBuckets();
                if(!CollectionUtils.isEmpty(buckets3)){
                    //ES中桶自定义的结构，就是一个桶一个key，这里是把buckets集合的所有桶中的唯一key全部取出来，装进list
                    List<String> collect = buckets3.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                    searchResponseAttrVo.setAttrValues(collect);
                }
                return searchResponseAttrVo;
            }).collect(Collectors.toList());
            responseVo.setFilters(searchResponseAttrVos);
        }

        return responseVo;
    }

    //解耦合，专门写一个创建SearchSourceBuilder的方法
    private SearchSourceBuilder buildDsl(SearchParamVo searchParamVo){

        //DSL：GET /goods/_search
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        String keyword = searchParamVo.getKeyword();

        if (StringUtils.isBlank(keyword)){
            //TODO：打广告
            return searchSourceBuilder;
        }

        //1. 构建检索条件（bool：must --> 让match和filter是and的关系）
        //DSL：query-->bool
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        searchSourceBuilder.query(boolQueryBuilder);

        //1.1 构建匹配的搜索条件：match
        //DSL：bool-->must[{match:title}]
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("title", keyword);
        matchQueryBuilder.operator(Operator.AND);
        boolQueryBuilder.must(matchQueryBuilder);

        //1.2 构建过滤条件：filter
        //1.2.1 构建品牌的过滤：brand
        List<Long> brandId = searchParamVo.getBrandId();
        //DSL：bool-->must[{match:title},filter[brandId]
        if(CollectionUtils.isEmpty(brandId)){
            TermsQueryBuilder termsQueryBuilder = QueryBuilders.termsQuery("brandId", brandId);
            boolQueryBuilder.filter(termsQueryBuilder);
        }

        //1.2.2 构建分类的过滤：category
        List<Long> categoryId = searchParamVo.getCategoryId();
        //DSL：bool-->must[{match:title},filter[brandId,categoryId]
        if(CollectionUtils.isEmpty(categoryId)){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryId", categoryId);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //1.2.3 构建价格区间的过滤：priceFrom，priceTo
        Double priceFrom = searchParamVo.getPriceFrom();
        Double priceTo = searchParamVo.getPriceTo();
        if(priceFrom != null || priceTo != null){
            RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
            if(priceFrom != null){
                rangeQueryBuilder.gte(priceFrom);
            }
            if(priceTo != null){
                rangeQueryBuilder.lte(priceTo);
            }
            boolQueryBuilder.filter(rangeQueryBuilder);
        }

        //1.2.4 构建是否有货的过滤：store
        Boolean store = searchParamVo.getStore();
        if(store != null && store){
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("store", store);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //1.2.5 构建规格参数的嵌套过滤
        List<String> props = searchParamVo.getProps();
        if(!CollectionUtils.isEmpty(props)){
            props.forEach(prop->{
                //each prop is like：4:8G-12G or 5:128G-256G-512G or 6:麒麟-骁龙 etc
                //DSL：根据DSL，嵌套nested查询中是bool查询，所以每个prop都要一个
                BoolQueryBuilder innerBoolQueryBuilder = QueryBuilders.boolQuery();
                //用:为标识，把prop字符串分割出attrId和attrValue两个字符串
                String[] split = StringUtils.split(prop, ":");
                //防止用户乱写，prop分割出来的split只可能长度为2，所以不是2的都不操作
                if(split != null && split.length == 2){
                    //分割后的第一部分：attrId，比如：4
                    innerBoolQueryBuilder.must(QueryBuilders.termQuery("searchAttrs.attrId", split[0]));
                    //分割后的第二部分：attrValue，比如：8G-12G
                    String[] attrValues = StringUtils.split(split[1], "-");
                    innerBoolQueryBuilder.must(QueryBuilders.termsQuery("searchAttrs.attrValue", attrValues));
                    //每个prop都会对应一个嵌套过滤：
                    //参数1. path：对应嵌套中的path，填入是什么属性加了@Field(type=FieldType.Nested)注解
                    //参数2. query：对应嵌套中的query查询，填入查询语句
                    //参数3. scoreMode：枚举，填入得分模式（这里是嵌套的属性作为过滤条件，过滤条件不影响得分，所以设置为None）
                    NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("searchAttrs", innerBoolQueryBuilder, ScoreMode.None);
                    boolQueryBuilder.filter(nestedQueryBuilder);
                }
            });
        }

        //2. 排序：sort
        Integer sort = searchParamVo.getSort();
        switch (sort){
            case 1: searchSourceBuilder.sort("price", SortOrder.DESC); break;
            case 2: searchSourceBuilder.sort("price",SortOrder.ASC); break;
            case 3: searchSourceBuilder.sort("sales", SortOrder.DESC); break;
            case 4: searchSourceBuilder.sort("createTime",SortOrder.DESC); break;
            default:
                searchSourceBuilder.sort("_score",SortOrder.DESC); break;
        }

        //3. 构建分页条件：from，size
        Integer pageNum = searchParamVo.getPageNum();
        Integer pageSize = searchParamVo.getPageSize();
        searchSourceBuilder.from((pageNum-1)*pageSize);
        searchSourceBuilder.size(pageSize);

        //4. 构建高亮：highlight
        searchSourceBuilder.highlighter(new HighlightBuilder()
                .field("title")
                .preTags("<font style='color:red;'>")
                .postTags("</font>"));

        //5. 构建聚合：aggs
        //5.1 构建品牌聚合：brandIdAgg
        searchSourceBuilder
                .aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId")
                        .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                        .subAggregation(AggregationBuilders.terms("brandImageAgg").field("brandImage")));

        //5.2 构建分类聚合：categoryIdAgg
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId")
                .subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));

        //5.3 构建规格参数聚合：attrAgg
        searchSourceBuilder
                .aggregation(AggregationBuilders.nested("attrAgg", "searchAttrs")
                        .subAggregation(AggregationBuilders.terms("attrIdAgg").field("searchAttrs.attrId")
                                .subAggregation(AggregationBuilders.terms("attrNameAgg").field("searchAttrs.attrName"))
                                .subAggregation(AggregationBuilders.terms("attrValueAgg").field("searchAttrs.attrValue"))));

        //6. 构建结果集
        searchSourceBuilder.fetchSource(new String[]{"skuId","defaultImage","price","title","subTitle"}, null);

        System.out.println(searchSourceBuilder);
        return searchSourceBuilder;
    }
}
