package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchParamVo;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import java.io.IOException;
import java.util.List;

@Service
public class SearchService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    public void search(SearchParamVo searchParamVo) {
        try {
            SearchRequest searchRequest = new SearchRequest(new String[]{"goods"}, buildDsl(searchParamVo));
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //TODO：解析搜索的结果集searchResponse
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //解耦合，专门写一个创建SearchSourceBuilder的方法
    public SearchSourceBuilder buildDsl(SearchParamVo searchParamVo){

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
                    QueryBuilders.nestedQuery("searchAttrs",innerBoolQueryBuilder, ScoreMode.None);
                    boolQueryBuilder.filter();
                }
            });
        }

        //2. 排序：sort

        //3. 构建分页条件：from，size

        //4. 构建高亮：highlight

        //5. 构建聚合：aggs
        //5.1 构建品牌聚合：brandIdAgg
        //5.2 构建分类聚合：categoryIdAgg
        //5.3 构建规格参数聚合：attrAgg


        System.out.println(searchSourceBuilder);
        return searchSourceBuilder;
    }
}
