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
    
    // es????????????
    // xinyuam/ElasticSearch/tree/master/source/spring-boot-demo-elasticsearch (??????????????????????????????)
    static class ESClient {
        public static RestHighLevelClient getClient(){
            // ??????es????????????ip,??????
            HttpHost httpHost = new HttpHost("192.168.10.106",9200);
            RestClientBuilder builder = RestClient.builder(httpHost);
            RestHighLevelClient client = new RestHighLevelClient(builder);
            return client;
        }
    }


    /**
     * ????????????
     */
    class QueryTest {
        private final String index = "sms-logs-index";
        private final String type = "sms-logs-type";
        // client??????
        private final RestHighLevelClient client = ESClient.getClient();
    
        // term??????
        public void termQuery() throws IOException {
            //1  request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            //2 ??????????????????
                // ??????form ,size
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.from(0);
            builder.size(5);
                 // ??????????????????,province????????????????????????
            builder.query(QueryBuilders.termQuery("province","??????"));
            request.source(builder);
            //3????????????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            //4 ???????????????
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // terms??????
        public void termsQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.termsQuery("province","??????","??????"));
            request.source(builder);
            // ????????????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // match_all??????
        public void matchAllQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchAllQuery());
            // builder.size(20); ????????????????????????????????????es???????????????10???
            request.source(builder);
            // ????????????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // match??????
        public void matchQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchQuery("smsContent","????????????"));
            request.source(builder);
            // ????????????
            SearchResponse response = client.search(request,RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // ??????match??????
        public void booleanMatchQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchQuery("smsContent","?????? ??????").operator(Operator.AND));
            request.source(builder);
            // ????????????
            SearchResponse response = client.search(request,RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
    
        // multi_match??????
        public void multiMatchQuery() throws IOException {
            // request
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.multiMatchQuery("??????","smsContent","province"));
            request.source(builder);
            // ????????????
            SearchResponse response = client.search(request,RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                Map<String, Object> result = hit.getSourceAsMap();
                System.out.println(result);
            }
        }
    
        // id??????
        public void idQuery() throws IOException {
            // ??????getRequest
            GetRequest request = new GetRequest(index,type,"1");
            // ????????????
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            // ????????????
            Map<String, Object> result = response.getSourceAsMap();
            System.out.println(result);
        }
    
        // ids??????
        public void idsQuery() throws IOException {
            // ????????????????????????????????????searchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.idsQuery().addIds("1","2","3"));
    
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // prefix??????
        public void prefixQuery() throws IOException {
            // ????????????SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.prefixQuery("corpName","??????"));
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // fuzzy??????
        public void fuzzyQuery() throws IOException {
            // ????????????SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.fuzzyQuery("corpName","???????????????"));
            //builder.query(QueryBuilders.fuzzyQuery("corpName","???????????????").prefixLength(2));
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // wildcard??????
        public void wildcardQuery() throws IOException {
            // ????????????SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.wildcardQuery("corpName","????????"));
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // range??????
        public void rangeQuery() throws IOException {
            // ????????????SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.rangeQuery("fee").gte(5).lte(10));
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
        // regexp??????
        public void regexpQuery() throws IOException {
            // ????????????SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.regexpQuery("mobile","15[0-9]{8}"));
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // scroll??????
        public void scrollQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????scroll????????????????????????1??????
            request.scroll(TimeValue.timeValueMinutes(1L));
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.size(1);
            builder.sort("fee", SortOrder.ASC);
            builder.query(QueryBuilders.matchAllQuery());
            // ???SearchSourceBuilder??????Request?????????????????????
            request.source(builder);
            // ??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // ???????????????????????????????????????scrollId
            String scrollId = response.getScrollId();
            System.out.println("------?????????------");
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
    
            // ?????????????????????
            while (true){
                // SearchScrollRequest,??????????????????,scrollId
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(TimeValue.timeValueMinutes(1L));
                // ????????????
                SearchResponse scrollResponse = client.scroll(scrollRequest, RequestOptions.DEFAULT);
    
                // ????????????????????????
                SearchHit[] hits = scrollResponse.getHits().getHits();
                if (hits !=null && hits.length>0){
                    System.out.println("------?????????------");
                    for (SearchHit hit : hits) {
                        System.out.println(hit.getSourceAsMap());
                    }
                }else {
                    System.out.println("-----????????????-----");
                    break;
                }
            }
            // ClearScrollRequest
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            // ??????ScoreId
            ClearScrollResponse scrollResponse = client.clearScroll(clearScrollRequest, RequestOptions.DEFAULT);
    
            System.out.println("??????scroll???????????????"+scrollResponse.isSucceeded());
        }
    
        // deleteByQuery??????
        public void deleteByQuery() throws IOException {
            // DeleteByQueryRequest
            DeleteByQueryRequest request = new DeleteByQueryRequest(index);
            request.types(type);
    
            // ??????????????????
            request.setQuery(QueryBuilders.rangeQuery("fee").lt(4));
    
            // ????????????
            BulkByScrollResponse response = client.deleteByQuery(request, RequestOptions.DEFAULT);
    
            System.out.println(response);
        }
    
        // boolQuery??????
        public void boolQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            // ??????????????????
            boolQuery.should(QueryBuilders.termQuery("province","??????"));
            boolQuery.should(QueryBuilders.termQuery("province","??????"));
            // ?????????????????????
            boolQuery.mustNot(QueryBuilders.termQuery("operatorId",2));
            // ?????????????????????
            boolQuery.must(QueryBuilders.matchQuery("smsContent","??????"));
            boolQuery.must(QueryBuilders.matchQuery("smsContent","??????"));
            // ????????????bool??????
            builder.query(boolQuery);
            request.source(builder);
    
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // boostingQuery??????
        public void boostingQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoostingQueryBuilder boostingQueryBuilder = QueryBuilders.boostingQuery(
                    QueryBuilders.matchQuery("smsContent", "?????????"),
                    QueryBuilders.matchQuery("smsContent", "??????")
            ).negativeBoost(0.5f);
            builder.query(boostingQueryBuilder);
            request.source(builder);
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // filterQuery??????
        public void filterQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            boolQuery.filter(QueryBuilders.termQuery("corpName","????????????"));
            boolQuery.filter(QueryBuilders.rangeQuery("fee").lte(5));
    
            builder.query(boolQuery);
            request.source(builder);
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
        // highlightQuery??????
        public void highlightQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(QueryBuilders.matchQuery("smsContent", "?????????"));
            // ????????????
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("smsContent",10) // ?????????10??????
            .preTags("<font color='read'>").postTags("</font>");    // ????????????
    
            builder.highlighter(highlightBuilder);
            request.source(builder);
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????,??????????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getHighlightFields());
            }
        }
    
        // ??????????????????
        public void cardinalityQuery() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.aggregation(AggregationBuilders.cardinality("agg").field("province"));
            request.source(builder);
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????,????????????,??????Aggregation?????????????????????????????????????????????????????????????????????????????????value
            Cardinality agg = response.getAggregations().get("agg");
            long value = agg.getValue();
            System.out.println("??????????????????"+value);
    
            // ?????????????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    
    
        // ??????????????????
        public void range() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.aggregation(AggregationBuilders.range("agg").field("fee")
                    .addUnboundedTo(5)   // ????????????
                    .addRange(5,10)
                    .addUnboundedFrom(10));
    
            request.source(builder);
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????
            Range agg = response.getAggregations().get("agg");
            for (Range.Bucket bucket : agg.getBuckets()) {
                String key = bucket.getKeyAsString();
                Object from = bucket.getFrom();
                Object to = bucket.getTo();
                long docCount = bucket.getDocCount();
                System.out.println(String.format("key???%s???from???%s???to???%s???docCount???%s",key,from,to,docCount));
            }
        }
    
        // ????????????       
        public void extendedStats() throws IOException {
            // SearchRequest
            SearchRequest request = new SearchRequest(index);
            request.types(type);
    
            // ??????????????????
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.aggregation(AggregationBuilders.extendedStats("agg").field("fee"));
    
            request.source(builder);
            // client??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
    
            // ????????????
            ExtendedStats agg = response.getAggregations().get("agg");
            double max = agg.getMax();
            double min = agg.getMin();
    
            System.out.println("fee???????????????"+max);
            System.out.println("fee???????????????"+min);
        }
        
        public void  GeoPolygon() throws IOException {
            //  1.??????searchRequest
            SearchRequest request  = new SearchRequest(index);
            request.types(type);
    
            //  2.?????? ????????????
            SearchSourceBuilder builder =  new SearchSourceBuilder();
            List<GeoPoint> points = new ArrayList<>();
            points.add(new GeoPoint(40.075013,116.220296));
            points.add(new GeoPoint(40.044751,116.346777));
            points.add(new GeoPoint(39.981533,116.236106));
            builder.query(QueryBuilders.geoPolygonQuery("location",points));
            request.source(builder);
            // 3.??????
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            // 4.????????????
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getSourceAsMap());
            }
        }
    }


    // ????????????????????????doc???????????????
    class CreateIndex {
        private String index = "sms-logs-index";
        private String type = "sms-logs-type";
        // client??????
        private RestHighLevelClient client = ESClient.getClient();

        // ????????????
        public void CreateIndexForSms() throws IOException {
            // ????????????
            Settings.Builder settings = Settings.builder()
                    .put("number_of_shards", 5)
                    .put("number_of_replicas", 1);
            // ??????mappings
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

            // ???settings???mappings?????????Request??????
            CreateIndexRequest request = new CreateIndexRequest(index)
                    .settings(settings)
                    .mapping(type,mappings);
            // ??????Client??????
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(res.toString());
        }
        
        // ????????????
        public void CreateTestData() throws IOException {
            // ????????????json??????
            SmsLogs s1 = new SmsLogs("1",new Date(),new Date(),"10690000988","1370000001","????????????","????????????????????????????????????????????????????????????????????????(Th12345678)",0,1,"??????","10.126.2.9",10,3);
            SmsLogs s2 = new SmsLogs("2",new Date(),new Date(),"84690110988","1570880001","????????????","??????????????????????????????????????????????????????,??????????????????,?????????:???????????????:15300000001",0,1,"??????","10.126.2.8",13,5);
            SmsLogs s3 = new SmsLogs("3",new Date(),new Date(),"10698880988","1593570001","????????????","????????????????????????????????????????????????1000???,??????????????????,??????????????????,??????:??????????????????:13890024793",0,1,"??????","10.126.2.7",12,10);
            SmsLogs s4 = new SmsLogs("4",new Date(),new Date(),"20697000911","1586890005","????????????","??????????????????????????????????????????????????????100???????????????????????????????????????????????????125???,2020???12???18???14:35",0,1,"??????","10.126.2.6",11,4);
            SmsLogs s5 = new SmsLogs("5",new Date(),new Date(),"18838880279","1562384869","??????","???????????????????????????,?????????????????????,?????????????????????????????????????????????,??????????????????---???????????????",0,1,"??????","10.126.2.5",10,2);
            // ??????json
            ObjectMapper mapper = new ObjectMapper();
            String json1 = mapper.writeValueAsString(s1);
            String json2 = mapper.writeValueAsString(s2);
            String json3 = mapper.writeValueAsString(s3);
            String json4 = mapper.writeValueAsString(s4);
            String json5 = mapper.writeValueAsString(s5);

            // request,?????????????????????
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest(index,type,s1.getId()).source(json1,XContentType.JSON));
            request.add(new IndexRequest(index,type,s2.getId()).source(json2,XContentType.JSON));
            request.add(new IndexRequest(index,type,s3.getId()).source(json3,XContentType.JSON));
            request.add(new IndexRequest(index,type,s4.getId()).source(json4,XContentType.JSON));
            request.add(new IndexRequest(index,type,s5.getId()).source(json5,XContentType.JSON));
            // client??????
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
        private Date createDate; // ????????????

        // @JsonFormat(pattern = "yyyy-MM-dd")
        private Date sendDate; // ????????????

        private String longCode; // ??????????????????
        private String mobile; // ?????????
        private String corpName;  // ??????????????????
        private String smsContent; // ????????????
        private Integer start; // ??????????????????,0?????????1??????
        private Integer operatorId; // ??????????????? 1?????? 2?????? 3??????
        private String province; // ??????
        private String ipAddr; // ?????????ip??????
        private Integer replyTotal; // ???????????????????????????????????????
        private Integer fee; // ??????

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
    
        // ??????????????????
        public void bulkCreateDoc() throws IOException {
            // ????????????json??????
            Person p1 = new Person(1,"??????",22,new Date());
            Person p2 = new Person(2,"??????",22,new Date());
            Person p3 = new Person(3,"??????",22,new Date());
            // ??????json
            ObjectMapper mapper = new ObjectMapper();
            String json1 = mapper.writeValueAsString(p1);
            String json2 = mapper.writeValueAsString(p2);
            String json3 = mapper.writeValueAsString(p2);
            // request,?????????????????????
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest(index,type,p1.getId().toString()).source(json1,XContentType.JSON));
            request.add(new IndexRequest(index,type,p2.getId().toString()).source(json2,XContentType.JSON));
            request.add(new IndexRequest(index,type,p3.getId().toString()).source(json3,XContentType.JSON));
            // client??????
            BulkResponse response = client.bulk(request, RequestOptions.DEFAULT);
    
        }
    
        // ????????????
        public void bulkDeleteDoc() throws IOException {
            BulkRequest request = new BulkRequest();
            // ???????????????doc???id?????????request
            request.add(new DeleteRequest(index,type,"1"));
            request.add(new DeleteRequest(index,type,"2"));
            request.add(new DeleteRequest(index,type,"3"));
            // client??????
            client.bulk(request,RequestOptions.DEFAULT);
    
        }
    
        // ????????????
        public void testConnect(){
            RestHighLevelClient client = ESClient.getClient();
            System.out.println("OK");
        }
    
        //????????????
        public void createIndex() throws IOException {
            // "number_of_shards": 5,      // ?????????
            //    "number_of_replicas": 1
            // ????????????
            Settings.Builder settings = Settings.builder()
                    .put("number_of_shards", 5)
                    .put("number_of_replicas", 1);
            // ?????????????????????mappings
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
            // ???settings???mappings?????????Request??????
            CreateIndexRequest request = new CreateIndexRequest(index)
                    .settings(settings)
                    .mapping(type,mappings);
            // ??????Client??????
            CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
            System.out.println(res.toString());
        }
    
    
        // ????????????????????????
        public  void findIndex() throws IOException {
            // ??????request??????
            GetIndexRequest request = new GetIndexRequest();
            request.indices(index);
    
            // ??????client????????????
            boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
            // ??????,
            System.out.println(exists);
        }
    
        // ????????????
        public  void deleteIndex() throws IOException {
            // ??????request??????
            DeleteIndexRequest request = new DeleteIndexRequest();
            request.indices(index);
    
            //??????client????????????
            AcknowledgedResponse delete = client.indices().delete(request, RequestOptions.DEFAULT);
    
            // ?????????????????????????????????
            System.out.println(delete.isAcknowledged());
        }
    
    
        // ????????????
        public void createDoc() throws IOException {
            // jackson
            ObjectMapper mapper = new ObjectMapper();
            // 1 ????????????json??????
            Person person = new Person(1,"??????",20,new Date());
            String json = mapper.writeValueAsString(person);
            // 2 request??????,????????????id?????????person?????????id
            IndexRequest request = new IndexRequest(index,type,person.getId().toString());
            request.source(json, XContentType.JSON);//???????????????????????????????????????json??????
            // 3 ??????client??????
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
    
            // 4 ???????????????????????????
            String result = response.getResult().toString();
            System.out.println(result); // ??????????????? CREATED
        }
    
        // ????????????
        public void updateDoc() throws IOException {
            // 1 ????????????map
            Map<String,Object> doc = new HashMap<>();
            doc.put("name","??????");
            String docId = "1";
            // 2 ????????????request????????????????????????????????????????????????index???type???doc???Id,????????????????????????doc
            UpdateRequest request = new UpdateRequest(index, type, docId);
            // ??????????????????????????????????????????map
            request.doc(doc);
            // 3 client????????????292A2B
            UpdateResponse update = client.update(request, RequestOptions.DEFAULT);
            // 4 ?????????????????????
            String result = update.getResult().toString();
            System.out.println(result);
        }
    
        // ????????????
        public void deleteDoc() throws IOException {
            // ??????request,??????????????????1?????????
            DeleteRequest request = new DeleteRequest(index, type, "1");
            // ??????client??????
            DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
            // ??????????????????
            String result = delete.getResult().toString();
            System.out.println(result); // ??????????????? DELETED
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
        <!--es?????????api-->
        <dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>6.5.3</version>
        </dependency>
*/