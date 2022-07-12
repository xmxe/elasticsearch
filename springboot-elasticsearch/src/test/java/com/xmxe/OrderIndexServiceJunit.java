package com.xmxe;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.ImmutableBiMap;
import com.xmxe.config.ElasticSearchClient;

@ActiveProfiles("dev")
@RunWith(SpringRunner.class)
@SpringBootTest
public class OrderIndexServiceJunit {

    private final Logger log = LoggerFactory.getLogger(OrderIndexServiceJunit.class);

    @Autowired
    private ElasticSearchClient elasticSearchClient;
    
    /**
     * 初始化索引结构
     *
     * @return
     */
    @Test
    public void initIndex(){
        String indexName = "orderIndex-2022-07";
        // 创建请求
        boolean existsIndex = elasticSearchClient.existsIndex(indexName);
        if (!existsIndex) {
            Map<String, Object> properties = buildMapping();
            elasticSearchClient.createIndex(indexName, properties);
        }
    }

    /**
     * 构建索引结构
     *
     * @return
     */
    private Map<String, Object> buildMapping() {
        Map<String, Object> properties = new HashMap();
        //订单id  唯一键ID
        properties.put("orderId", ImmutableBiMap.of("type", "keyword"));
        //订单号
        properties.put("orderNo", ImmutableBiMap.of("type", "keyword"));
        //客户姓名
        properties.put("orderUserName", ImmutableBiMap.of("type", "text"));
        
        //订单项
        Map<String, Object> orderItems = new HashMap();
        //订单项ID
        orderItems.put("orderItemId", ImmutableBiMap.of("type", "keyword"));
        //产品名称
        orderItems.put("productName", ImmutableBiMap.of("type", "text"));
        //品牌名称
        orderItems.put("brandName", ImmutableBiMap.of("type", "text"));
        //销售金额,单位分*100
        orderItems.put("sellPrice", ImmutableBiMap.of("type", "integer"));
        properties.put("orderItems", ImmutableBiMap.of("type", "nested", "properties", orderItems));

        //文档结构映射
        Map<String, Object> mapping = new HashMap();
        mapping.put("properties", properties);
        return mapping;
    }


     /**
     * 保存订单到ES中
     * @param request
     */
    @Test
    public void saveDocument(){
        String indexName =  "orderIndex-2022-07";
        //从数据库查询最新订单数据，并封装成对应的es订单结构
        String orderId = "202202020202";
        // OrderIndexDocDTO indexDocDTO = buildOrderIndexDocDTO(orderId);
        //保存数据到ES中
        elasticSearchClient.addDocument(indexName, orderId, "indexDocDTO");
    }

     /**
     * 通过商品、品牌、价格等条件，分页查询订单数据
     * @param request
     */
    @Test
    public void search1(){
        //查询索引，支持通配符
        String indexName = "orderIndex-*";
        String orderUserName = "张三";
        String productName = "薯条";
        // 条件搜索
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //组合搜索
        BoolQueryBuilder mainBoolQuery = new BoolQueryBuilder();
        mainBoolQuery.must(QueryBuilders.matchQuery("orderUserName", orderUserName));

        //订单项相关信息搜索
        BoolQueryBuilder nestedBoolQuery = new BoolQueryBuilder(); nestedBoolQuery.must(QueryBuilders.matchQuery("orderItems.productName", productName));
        //内嵌对象搜索，需要指定path
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("orderItems",nestedBoolQuery, ScoreMode.None);
        //子表查询
        mainBoolQuery.must(nestedQueryBuilder);

        //封装查询参数
        builder.query(mainBoolQuery);

        //返回参数
        builder.fetchSource(new String[]{}, new String[]{});

        //结果集合分页，从第一页开始，返回最多四条数据
        builder.from(0).size(4);

        //排序
        builder.sort("orderId", SortOrder.DESC);
        log.info("dsl：{}", builder.toString());
        // 执行请求
        SearchResponse response = elasticSearchClient.searchDocument(indexName, builder);
        // 当前返回的总行数
        long count = response.getHits().getTotalHits();
        // 返回的具体行数
        SearchHit[] searchHits = response.getHits().getHits();
        log.info("response：{}", response.toString());
    }


     /**
     * 通过订单ID、商品、品牌、价格等，分页查询订单项数据
     * @param request
     */
    @Test
    public void search2(){
        //查询索引，支持通配符
        String indexName = "orderIndex-*";
        String orderId = "202202020202";
        String productName = "薯条";
        // 条件搜索
        SearchSourceBuilder builder = new SearchSourceBuilder();

        //组合搜索
        BoolQueryBuilder mainBoolQuery = new BoolQueryBuilder();
        mainBoolQuery.must(QueryBuilders.termQuery("_id", orderId));

        //订单项相关信息搜索
        BoolQueryBuilder nestedBoolQuery = new BoolQueryBuilder(); nestedBoolQuery.must(QueryBuilders.matchQuery("orderItems.productName", productName));
        //内嵌对象搜索，需要指定path
        NestedQueryBuilder nestedQueryBuilder = QueryBuilders.nestedQuery("orderItems",nestedBoolQuery, ScoreMode.None);

        //内嵌对象分页查询
        InnerHitBuilder innerHitBuilder = new InnerHitBuilder();
        //结果集合分页，从第一页开始，返回最多四条数据
        innerHitBuilder.setFrom(0).setSize(4);
        //只返回订单项id
        innerHitBuilder.setFetchSourceContext(new FetchSourceContext(true, new String[]{"orderItems.orderItemId"}, new String[]{}));
        innerHitBuilder.addSort(SortBuilders.fieldSort("orderItems.orderItemId").order(SortOrder.DESC));
        nestedQueryBuilder.innerHit(innerHitBuilder);
        
        //子表查询
        mainBoolQuery.must(nestedQueryBuilder);

        //封装查询参数
        builder.query(mainBoolQuery);

        //返回参数
        builder.fetchSource(new String[]{}, new String[]{});

        //结果集合分页，从第一页开始，返回最多四条数据
        builder.from(0).size(4);

        //排序
        builder.sort("orderId", SortOrder.DESC);
        log.info("dsl：{}", builder.toString());
        // 执行请求
        SearchResponse response = elasticSearchClient.searchDocument(indexName, builder);
        // 当前返回的订单主表总行数
        long count = response.getHits().getTotalHits();
        // 返回的订单主表数据
        SearchHit[] searchHits = response.getHits().getHits();
        // 返回查询的的订单项分页数据
        Map<String, SearchHits> m = searchHits[0].getInnerHits();
        log.info("response：{}", response.toString());
    }

}


