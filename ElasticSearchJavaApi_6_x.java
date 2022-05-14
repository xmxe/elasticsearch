import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.*;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.range.Range;
import org.elasticsearch.search.aggregations.metrics.cardinality.Cardinality;
import org.elasticsearch.search.aggregations.metrics.stats.extended.ExtendedStats;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.common.xcontent.json.JsonXContent;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class ElasticSearchJavaApi_6_x {
    
    // es连接对象
    // xinyuam/ElasticSearch/tree/master/source/spring-boot-demo-elasticsearch (创建客户端的几种方式)
    static class ESClient {
        public static RestHighLevelClient getClient(){
            // 指定es服务器的ip,端口
            HttpHost httpHost = new HttpHost("192.168.10.106",9200);
            RestClientBuilder builder = RestClient.builder(httpHost);
            RestHighLevelClient client = new RestHighLevelClient(builder);
            return client;
        }
    }


    /**
     * 各种查询
     */
    class QueryTest {
        private final String index = "sms-logs-index";
        private final String type = "sms-logs-type";
        // client对象
        private final RestHighLevelClient client = ESClient.getClient();
    
        // term查询
        public void termQuery() throws IOException {
            //1  request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            //2 指定查询条件
                // 指定form ,size
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.from(0);
            builder.size(5);
                 // 指定查询条件,province字段，内容为北京
            builder.query(QueryBuilders.termQuery("province","上海"));
            request.source(builder);
            //3执行查询
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4 获取到数据
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // terms查询
        public void termsQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.termsQuery("province","上海","河南"));
            request.source(builder);
            // 执行查询
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取数据
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // match_all查询
        public void matchAllQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchAllQuery());
            // builder.size(20); 在这里指定要显示的个数，es默认只回显10条
            request.source(builder);
            // 执行查询
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取数据
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // match查询
        public void matchQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchQuery("smsContent","电话号码"));
            request.source(builder);
            // 执行查询
            SearchResponse response = client.search(request,RequestOptions.DEFAULT);
            // 获取数据
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // 布尔match查询
        public void booleanMatchQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchQuery("smsContent","电话 快递").operator(Operator.AND));
            request.source(builder);
            // 执行查询
            SearchResponse response = client.search(request,RequestOptions.DEFAULT);
            // 获取数据
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
    
        // multi_match查询
        public void multiMatchQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.multiMatchQuery("中国","smsContent","province"));
            request.source(builder);
            // 执行查询
            SearchResponse response = client.search(request,RequestOptions.DEFAULT);
            // 获取数据
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // id查询
        public void idQuery() throws IOException {
            // 使用getRequest
            GetRequest request = new GetRequest(index,type,"1");
            // 执行查询
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            // 输出结果
            Map<String, Object> result = response.getSourceAsMap();
            System.out.println(result);
        }
    
        // ids查询
        public void idsQuery() throws IOException {
            // 这个属于复杂查询需要使用searchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.idsQuery().addIds("1","2","3"));
    
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 输出结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // prefix查询
        public void prefixQuery() throws IOException {
            // 依然使用SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.prefixQuery("corpName","滴滴"));
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // fuzzy查询
        public void fuzzyQuery() throws IOException {
            // 依然使用SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.fuzzyQuery("corpName","中国移不动"));
            //builder.query(QueryBuilders.fuzzyQuery("corpName","中国移不动").prefixLength(2));
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // wildcard查询
        public void wildcardQuery() throws IOException {
            // 依然使用SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.wildcardQuery("corpName","中国??"));
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // range查询
        public void rangeQuery() throws IOException {
            // 依然使用SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.rangeQuery("fee").gte(5).lte(10));
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
        // regexp查询
        public void regexpQuery() throws IOException {
            // 依然使用SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 查询
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.regexpQuery("mobile","15[0-9]{8}"));
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // scroll查询
        public void scrollQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 指定scroll的信息，存在内存1分钟
            request.scroll(TimeValue.timeValueMinutes(1L));
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.size(1);
            builder.sort("fee", SortOrder.ASC);
            builder.query(QueryBuilders.matchAllQuery());
            // 把SearchSourceBuilder放到Request中，千万别忘了
            request.source(builder);
            // 执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 获取第一页的结果结果，以及scrollId
            String scrollId = response.getScrollId();
            System.out.println("------第一页------");
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
    
            // 循环遍历其余页
            while (true){
                // SearchScrollRequest,指定生存时间,scrollId
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
                // 执行查询
                SearchResponse scrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
    
                // 如果查询到了数据
                SearchHit[] hits = scrollResponse.getHits().getHits();
                if (hits !=null && hits.length>0){
                    System.out.println("------下一页------");
                    for (SearchHit hit : hits) {
                        System.out.println(hit.getSourceAsMap());
                    }
                }else {
                    System.out.println("-----最后一页-----");
                    break;
                }
            }
            // ClearScrollRequest
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            // 删除ScoreId
            ClearScrollResponse scrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
    
            System.out.println("删除scroll成功了吗？"+scrollResponse.isSucceeded());
        }
    
        // deleteByQuery查询
        public void deleteByQuery() throws IOException {
            // DeleteByQueryRequest
            DeleteByQueryRequest request = new DeleteByQueryRequest(index);
            request.types(type);
    
            // 指定检索条件
            request.setQuery(QueryBuilders.rangeQuery("fee").lt(4));
    
            // 执行删除
            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
    
            System.out.println(response);
        }
    
        // boolQuery查询
        public void boolQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // 上海或者河南
            boolQuery.should(QueryBuilders.termQuery("province","武汉"));
            boolQuery.should(QueryBuilders.termQuery("province","河南"));
            // 运营商不是联通
            boolQuery.mustNot(QueryBuilders.termQuery("operatorId",2));
            // 包含中国和移动
            boolQuery.must(QueryBuilders.matchQuery("smsContent","中国"));
            boolQuery.must(QueryBuilders.matchQuery("smsContent","移动"));
            // 指定使用bool查询
            builder.query(boolQuery);
            request.source(builder);
    
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // boostingQuery查询
        public void boostingQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoostingQueryBuilder boostingQueryBuilder = QueryBuilders.boostingQuery(
                    QueryBuilders.matchQuery("smsContent", "亲爱的"),
                    QueryBuilders.matchQuery("smsContent", "网易")
            ).negativeBoost(0.5f);
            builder.query(boostingQueryBuilder);
            request.source(builder);
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // filterQuery查询
        public void filterQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.filter(QueryBuilders.termQuery("corpName","中国移动"));
            boolQuery.filter(QueryBuilders.rangeQuery("fee").lte(5));
    
            builder.query(boolQuery);
            request.source(builder);
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // highlightQuery查询
        public void highlightQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchQuery("smsContent", "亲爱的"));
            // 高亮显示
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("smsContent",10) // 只显示10个字
            .preTags("<font color='read'>").postTags("</font>");    // 红色展示
    
            builder.highlighter(highlightBuilder);
            request.source(builder);
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果,拿高亮的内容
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getHighlightFields());
            }
        }
    
        // 去重记数查询
        public void cardinalityQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.aggregation(AggregationBuilders.cardinality("agg").field("province"));
            request.source(builder);
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果,拿到总数,因为Aggregation是一个接口，我们需要向下转型，使用实现类的方法才能拿的value
            Cardinality agg = response.getAggregations().get("agg");
            long value = agg.getValue();
            System.out.println("省份总数为："+value);
    
            // 拿到查询的内容
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
    
        // 范围统计查询
        public void range() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.aggregation(AggregationBuilders.range("agg").field("fee")
                    .addUnboundedTo(5)   // 指定范围
                    .addRange(5,10)
                    .addUnboundedFrom(10));
    
            request.source(builder);
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果
            Range agg = response.getAggregations().get("agg");
            for (Range.Bucket bucket : agg.getBuckets()) {
                String key = bucket.getKeyAsString();
                Object from = bucket.getFrom();
                Object to = bucket.getTo();
                long docCount = bucket.getDocCount();
                System.out.println(String.format("key：%s，from：%s，to：%s，docCount：%s",key,from,to,docCount));
            }
        }
    
        // 聚合查询       
        public void extendedStats() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // 指定查询条件
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.aggregation(AggregationBuilders.extendedStats("agg").field("fee"));
    
            request.source(builder);
            // client执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // 获取结果
            ExtendedStats agg = response.getAggregations().get("agg");
            double max = agg.getMax();
            double min = agg.getMin();
    
            System.out.println("fee的最大值为"+max);
            System.out.println("fee的最小值为"+min);
        }
        
        public void  GeoPolygon() throws IOException {
            //  1.创建searchRequest
            SearchRequest request  = new SearchRequest(index);
            request.types(type);
    
            //  2.指定 检索方式
            SearchSourceBuilder builder =  new SearchSourceBuilder();
            List<GeoPoint> points = new ArrayList<>();
            points.add(new GeoPoint(40.075013,116.220296));
            points.add(new GeoPoint(40.044751,116.346777));
            points.add(new GeoPoint(39.981533,116.236106));
            builder.query(QueryBuilders.geoPolygonQuery("location",points));
            request.source(builder);
            // 3.执行
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4.输出结果
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    }


    // 创建索引，并指定doc的测试数据
    class CreateIndex {
        private String index = "sms-logs-index";
        private String type = "sms-logs-type";
        // client对象
        private RestHighLevelClient client = ESClient.getClient();

        // 创建索引
        public void CreateIndexForSms() throws IOException {
            // 创建索引
            Settings.Builder settings = Settings.builder()
                    .put("number_of_shards", 5)
                    .put("number_of_replicas", 1);
            // 指定mappings
            XContentBuilder mappings = JsonXContent.contentBuilder()
                    .startObject()
                        .startObject("properties")
                            .startObject("createDate")
                                .field("type", "date")
                                .field("format","yyyy-MM-dd")
                            .endObject()
                            .startObject("sendDate")
                                .field("type", "date")
                                .field("format", "yyyy-MM-dd")
                            .endObject()
                                .startObject("longCode")
                                .field("type", "keyword")
                            .endObject()
                                .startObject("mobile")
                                .field("type", "keyword")
                            .endObject()
                                .startObject("corpName")
                                .field("type", "keyword")
                            .endObject()
                                .startObject("smsContent")
                                .field("type", "text")
                                .field("analyzer", "ik_max_word")
                            .endObject()
                                .startObject("state")
                                .field("type", "integer")
                            .endObject()
                                .startObject("operatorId")
                                .field("type", "integer")
                            .endObject()
                                .startObject("province")
                                .field("type", "keyword")
                            .endObject()
                                .startObject("ipAddr")
                                .field("type", "ip")
                            .endObject()
                                .startObject("replyTotal")
                                .field("type", "integer")
                            .endObject()
                                .startObject("fee")
                                .field("type", "long")
                            .endObject()
                        .endObject()
                    .endObject();

            // 将settings和mappings封装为Request对象
            CreateIndexRequest request = new CreateIndexRequest(index)
                    .settings(settings)
                    .mapping(type,mappings);
            // 通过Client连接
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(res.toString());
        }
        
        // 测试数据
        public void CreateTestData() throws IOException {
            // 准备多个json数据
            SmsLogs s1 = new SmsLogs("1",new Date(),new Date(),"10690000988","1370000001","途虎养车","【途虎养车】亲爱的刘女士，您在途虎购买的货物单号(Th12345678)",0,1,"上海","10.126.2.9",10,3);
            SmsLogs s2 = new SmsLogs("2",new Date(),new Date(),"84690110988","1570880001","韵达快递","【韵达快递】您的订单已配送不要走开哦,很快就会到了,配送员:王五，电话:15300000001",0,1,"上海","10.126.2.8",13,5);
            SmsLogs s3 = new SmsLogs("3",new Date(),new Date(),"10698880988","1593570001","滴滴打车","【滴滴打车】指定的车辆现在距离您1000米,马上就要到了,请耐心等待哦,司机:李师傅，电话:13890024793",0,1,"河南","10.126.2.7",12,10);
            SmsLogs s4 = new SmsLogs("4",new Date(),new Date(),"20697000911","1586890005","中国移动","【中国移动】尊敬的客户，您充值的话费100元，现已经成功到账，您的当前余额为125元,2020年12月18日14:35",0,1,"北京","10.126.2.6",11,4);
            SmsLogs s5 = new SmsLogs("5",new Date(),new Date(),"18838880279","1562384869","网易","【网易】亲爱的玩家,您已经排队成功,请尽快登录到网易云游戏进行游玩,祝您游戏愉快---网易云游戏",0,1,"杭州","10.126.2.5",10,2);
            // 转为json
            ObjectMapper mapper = new ObjectMapper();
            String json1 = mapper.writeValueAsString(s1);
            String json2 = mapper.writeValueAsString(s2);
            String json3 = mapper.writeValueAsString(s3);
            String json4 = mapper.writeValueAsString(s4);
            String json5 = mapper.writeValueAsString(s5);

            // request,将数据封装进去
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest(index,type,s1.getId()).source(json1,XContentType.JSON));
            request.add(new IndexRequest(index,type,s2.getId()).source(json2,XContentType.JSON));
            request.add(new IndexRequest(index,type,s3.getId()).source(json3,XContentType.JSON));
            request.add(new IndexRequest(index,type,s4.getId()).source(json4,XContentType.JSON));
            request.add(new IndexRequest(index,type,s5.getId()).source(json5,XContentType.JSON));
            // client执行
            BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
            System.out.println(response);
        }
    }

    class Person {
        // @JsonIgnore
        private Integer id;
        private String name;
        private Integer age;
        // @JsonFormat(pattern = "yyyy-MM-dd")
        private Date birthday;


        public Person(Integer id, String name, Integer age, Date birthday) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.birthday = birthday;
        }

        public Person() {
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public Date getBirthday() {
            return birthday;
        }

        public void setBirthday(Date birthday) {
            this.birthday = birthday;
        }
    }

    class SmsLogs {
        // @JsonIgnore
        private String id; // id

        // @JsonFormat(pattern = "yyyy-MM-dd")
        private Date createDate; // 创建时间

        // @JsonFormat(pattern = "yyyy-MM-dd")
        private Date sendDate; // 发送时间

        private String longCode; // 发送的长号码
        private String mobile; // 手机号
        private String corpName;  // 发送公司名称
        private String smsContent; // 短信内容
        private Integer start; // 短信发送状态,0成功，1失败
        private Integer operatorId; // 运营商编号 1移动 2联通 3电信
        private String province; // 省份
        private String ipAddr; // 服务器ip地址
        private Integer replyTotal; // 短信状态报告返回时长（秒）
        private Integer fee; // 费用

        public SmsLogs() {
        }

        public SmsLogs(String id, Date createDate, Date sendDate, String longCode, String mobile, String corpName, String smsContent, Integer start, Integer operatorId, String province, String ipAddr, Integer replyTotal, Integer fee) {
            this.id = id;
            this.createDate = createDate;
            this.sendDate = sendDate;
            this.longCode = longCode;
            this.mobile = mobile;
            this.corpName = corpName;
            this.smsContent = smsContent;
            this.start = start;
            this.operatorId = operatorId;
            this.province = province;
            this.ipAddr = ipAddr;
            this.replyTotal = replyTotal;
            this.fee = fee;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Date getCreateDate() {
            return createDate;
        }

        public void setCreateDate(Date createDate) {
            this.createDate = createDate;
        }

        public Date getSendDate() {
            return sendDate;
        }

        public void setSendDate(Date sendDate) {
            this.sendDate = sendDate;
        }

        public String getLongCode() {
            return longCode;
        }

        public void setLongCode(String longCode) {
            this.longCode = longCode;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getCorpName() {
            return corpName;
        }

        public void setCorpName(String corpName) {
            this.corpName = corpName;
        }

        public String getSmsContent() {
            return smsContent;
        }

        public void setSmsContent(String smsContent) {
            this.smsContent = smsContent;
        }

        public Integer getStart() {
            return start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public Integer getOperatorId() {
            return operatorId;
        }

        public void setOperatorId(Integer operatorId) {
            this.operatorId = operatorId;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getIpAddr() {
            return ipAddr;
        }

        public void setIpAddr(String ipAddr) {
            this.ipAddr = ipAddr;
        }

        public Integer getReplyTotal() {
            return replyTotal;
        }

        public void setReplyTotal(Integer replyTotal) {
            this.replyTotal = replyTotal;
        }

        public Integer getFee() {
            return fee;
        }

        public void setFee(Integer fee) {
            this.fee = fee;
        }
    }


    class Demo {
        private RestHighLevelClient client = ESClient.getClient();
        private String index = "person";
        private String type = "man";
    
        // 创建批量操作
        public void bulkCreateDoc() throws IOException {
            // 准备多个json数据
            Person p1 = new Person(1,"张三",22,new Date());
            Person p2 = new Person(2,"李四",22,new Date());
            Person p3 = new Person(3,"王五",22,new Date());
            // 转为json
            ObjectMapper mapper = new ObjectMapper();
            String json1 = mapper.writeValueAsString(p1);
            String json2 = mapper.writeValueAsString(p2);
            String json3 = mapper.writeValueAsString(p2);
            // request,将数据封装进去
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest(index,type,p1.getId().toString()).source(json1,XContentType.JSON));
            request.add(new IndexRequest(index,type,p2.getId().toString()).source(json2,XContentType.JSON));
            request.add(new IndexRequest(index,type,p3.getId().toString()).source(json3,XContentType.JSON));
            // client执行
            BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
    
        }
    
        // 批量删除
        public void bulkDeleteDoc() throws IOException {
            BulkRequest request = new BulkRequest();
            // 将要删除的doc的id添加到request
            request.add(new DeleteRequest(index,type,"1"));
            request.add(new DeleteRequest(index,type,"2"));
            request.add(new DeleteRequest(index,type,"3"));
            // client执行
            client.bulk(request,RequestOptions.DEFAULT);
    
        }
    
        // 测试连接
        public void testConnect(){
            RestHighLevelClient client = ESClient.getClient();
            System.out.println("OK");
        }
    
        //创建索引
        public void createIndex() throws IOException {
            // "number_of_shards": 5,      // 分片数
            //    "number_of_replicas": 1
            // 创建索引
            Settings.Builder settings = Settings.builder()
                    .put("number_of_shards", 5)
                    .put("number_of_replicas", 1);
            // 准备索引的结构mappings
            XContentBuilder mappings = JsonXContent.contentBuilder()
                    .startObject()
                        .startObject("properties")
                            .startObject("name")
                               .field("type","text")
                            .endObject()
                            .startObject("age")
                               .field("type","integer")
                            .endObject()
                            .startObject("birthday")
                               .field("type","date")
                               .field("format","yyyy-MM-dd")
                            .endObject()
                        .endObject()
                    .endObject();
            // 将settings和mappings封装为Request对象
            CreateIndexRequest request = new CreateIndexRequest(index)
                    .settings(settings)
                    .mapping(type,mappings);
            // 通过Client连接
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(res.toString());
        }
    
    
        // 检查索引是否存在
        public  void findIndex() throws IOException {
            // 准备request对象
            GetIndexRequest request = new GetIndexRequest();
            request.indices(index);
    
            // 通过client对象操作
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            // 输出,
            System.out.println(exists);
        }
    
        // 删除索引
        public  void deleteIndex() throws IOException {
            // 准备request对象
            DeleteIndexRequest request = new DeleteIndexRequest();
            request.indices(index);
    
            //通过client对象操作
            AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
    
            // 拿的是否删除成功的结果
            System.out.println(delete.isAcknowledged());
        }
    
    
        // 文档创建
        public void createDoc() throws IOException {
            // jackson
            ObjectMapper mapper = new ObjectMapper();
            // 1 准备一个json数据
            Person person = new Person(1,"张三",20,new Date());
            String json = mapper.writeValueAsString(person);
            // 2 request对象,手动指定id，使用person对象的id
            IndexRequest request = new IndexRequest(index,type,person.getId().toString());
            request.source(json, XContentType.JSON);//第二个参数告诉他这个参数是json类型
            // 3 通过client操作
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
    
            // 4 创建成功返回的结果
            String result = response.getResult().toString();
            System.out.println(result); // 成功会返回 CREATED
        }
    
        // 文档修改
        public void updateDoc() throws IOException {
            // 1 创建一个map
            Map<String,Object> doc = new HashMap<>();
            doc.put("name","张三");
            String docId = "1";
            // 2 创建一个request对象，指定要修改哪个，这里指定了index，type和doc的Id,也就是确定唯一的doc
            UpdateRequest request = new UpdateRequest(index, type, docId);
            // 指定修改的内容，也就是上面的map
            request.doc(doc);
            // 3 client对象执行292A2B
            UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
            // 4 执行返回的结果
            String result = update.getResult().toString();
            System.out.println(result);
        }
    
        // 删除文档
        public void deleteDoc() throws IOException {
            // 创建request,指定我要删除1号文档
            DeleteRequest request = new DeleteRequest(index, type, "1");
            // 通过client执行
            DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
            // 获取执行结果
            String result = delete.getResult().toString();
            System.out.println(result); // 返回结果为 DELETED
        }
    
        
    }
}
/**
		<dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.11.1</version>
        </dependency>
        <!--es-->
        <dependency>
            <groupId>org.elasticsearch</groupId>
            <artifactId>elasticsearch</artifactId>
            <version>6.5.3</version>
        </dependency>
        <!--es的高级api-->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>6.5.3</version>
        </dependency>
*/