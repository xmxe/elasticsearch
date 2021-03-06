
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;


public class ElasticSearchJavaApi_7_8_0 {
    
    /**
     * ???????????????
     */
    public void helloElasticsearch() throws Exception{
        // ?????????????????????
		RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http")));
        //		...
        System.out.println(client);

        // ?????????????????????
        client.close();
    }

    // ---------------??????--------------

    /**
     * ????????????
     */
    public void createIndex() throws Exception{
        // ?????????????????????
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        // ???????????? - ????????????
        CreateIndexRequest request = new CreateIndexRequest("user2");
        // ???????????????????????????
        CreateIndexResponse response = client.indices().create(request,
                RequestOptions.DEFAULT);
        boolean acknowledged = response.isAcknowledged();
        // ????????????
        System.out.println("???????????? = " + acknowledged);

        // ?????????????????????
        client.close();
    }

    /**
     * ????????????
     */
    public void searchIndex()throws Exception{
        // ?????????????????????
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        // ???????????? - ????????????
        GetIndexRequest request = new GetIndexRequest("user2");
        // ???????????????????????????
        GetIndexResponse response = client.indices().get(request,
                RequestOptions.DEFAULT);
        
        System.out.println("aliases:"+response.getAliases());
        System.out.println("mappings:"+response.getMappings());
        System.out.println("settings:"+response.getSettings());

        client.close();
    }

    /**
     * ????????????
     */
    public void deleteIndex()throws Exception{
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
        // ???????????? - ????????????
        DeleteIndexRequest request = new DeleteIndexRequest("user2");
        // ???????????????????????????
        AcknowledgedResponse response = client.indices().delete(request,RequestOptions.DEFAULT);
        // ????????????
        System.out.println("???????????? ??? " + response.isAcknowledged());
        client.close();
    }


    // --------??????---------

    /**
     * ??????????????????
     * @param task ???????????????es???????????????s
     */
    public void connect(ElasticsearchTask task){
        // ?????????????????????
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
        try {
            task.doSomething(client);
            // ?????????????????????
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * ????????????
     */
    public void insertDoc(){
        this.connect(client -> {
            // ???????????? - ????????????
            IndexRequest request = new IndexRequest();
            // ??????????????????????????????
            request.index("user").id("1001");

            // ??????????????????
            User user = new User();
            user.setName("zhangsan");
            user.setAge(30);
            user.setSex("???");
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String productJson = objectMapper.writeValueAsString(user);
               
                // ???????????????????????????????????? JSON ??????
                request.source(productJson, XContentType.JSON);
                // ??????????????????????????????????????????
                IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                // ??????????????????
                System.out.println("_index:" + response.getIndex());
                System.out.println("_id:" + response.getId());
                System.out.println("_result:" + response.getResult());

            } catch (Exception e) {
                e.printStackTrace();
            }
            
        });
    }


    /**
     * ????????????
     */
    public void updateDoc(){
        this.connect(client -> {
            // ???????????? - ????????????
            UpdateRequest request = new UpdateRequest();
            // ??????????????????
            request.index("user").id("1001");
            // ???????????????????????????????????????
            request.doc(XContentType.JSON, "sex", "???");
            // ??????????????????????????????????????????
            UpdateResponse response;
            try {
                response = client.update(request, RequestOptions.DEFAULT);
                System.out.println("_index:" + response.getIndex());
                System.out.println("_id:" + response.getId());
                System.out.println("_result:" + response.getResult());
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        });
    }

    /**
     * ????????????
     */
    public void getDoc(){
        this.connect(client -> {
            //1.??????????????????
            GetRequest request = new GetRequest().index("user").id("1001");
            //2.??????????????????????????????????????????
            GetResponse response;
            try {
                response = client.get(request, RequestOptions.DEFAULT);
                // 3.??????????????????
                System.out.println("_index:" + response.getIndex());
                System.out.println("_type:" + response.getType());
                System.out.println("_id:" + response.getId());
                System.out.println("source:" + response.getSourceAsString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        });
    }

    /**
     * ????????????
     */
    public void deleteDOc(){
        this.connect(client -> {
            //??????????????????
            DeleteRequest request = new DeleteRequest().index("user").id("1001");
            //??????????????????????????????????????????
            DeleteResponse response;
            try {
                response = client.delete(request, RequestOptions.DEFAULT);
                 //????????????
                System.out.println(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        });
    }

    /**
     * ??????????????????
     */
    public void batchInsertDoc(){
        this.connect(client -> {
            //??????????????????????????????
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest().index("user").id("1001").source(XContentType.JSON, "name", "zhangsan"));
            request.add(new IndexRequest().index("user").id("1002").source(XContentType.JSON, "name", "lisi"));
            request.add(new IndexRequest().index("user").id("1003").source(XContentType.JSON, "name", "wangwu"));
            //??????????????????????????????????????????
            BulkResponse responses;
            try {
                responses = client.bulk(request, RequestOptions.DEFAULT);
                //??????????????????
                System.out.println("took:" + responses.getTook());
                System.out.println("items:" + responses.getItems());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        });
    }


    /**
     * ??????????????????
     */
    public void batchDeleteDoc(){
        this.connect(client -> {
            //??????????????????????????????
            BulkRequest request = new BulkRequest();
            request.add(new DeleteRequest().index("user").id("1001"));
            request.add(new DeleteRequest().index("user").id("1002"));
            request.add(new DeleteRequest().index("user").id("1003"));
            //??????????????????????????????????????????
            BulkResponse responses;
            try {
                responses = client.bulk(request, RequestOptions.DEFAULT);
                //??????????????????
                System.out.println("took:" + responses.getTook());
                System.out.println("items:" + responses.getItems());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        });
    }


    /**
     * ????????????????????????
     */
    public void queryDoc(){
        this.connect(client -> {
            // ????????????????????????
            SearchRequest request = new SearchRequest();
            request.indices("user");
            // ????????????????????????
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            // ??????????????????
            sourceBuilder.query(QueryBuilders.matchAllQuery());
            request.source(sourceBuilder);
            SearchResponse response;
            try {
                response = client.search(request, RequestOptions.DEFAULT);
                // ????????????
                SearchHits hits = response.getHits();
                System.out.println("took:" + response.getTook());
                System.out.println("timeout:" + response.isTimedOut());
                System.out.println("total:" + hits.getTotalHits());
                System.out.println("MaxScore:" + hits.getMaxScore());
                System.out.println("hits========>>");
                for (SearchHit hit : hits) {
                //?????????????????????????????????
                    System.out.println(hit.getSourceAsString());
                }
                System.out.println("<<========");
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        });
    }
    
    /**
     * ????????????
     */
    public void conditionSearch(){
        this.connect(client -> {
            // ????????????????????????
            SearchRequest request = new SearchRequest();
            request.indices("user");
            // ????????????????????????
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.termQuery("age", "30"));
            request.source(sourceBuilder);
            SearchResponse response;
            try {
                response = client.search(request, RequestOptions.DEFAULT);
                // ????????????
                SearchHits hits = response.getHits();
                System.out.println("took:" + response.getTook());
                System.out.println("timeout:" + response.isTimedOut());
                System.out.println("total:" + hits.getTotalHits());
                System.out.println("MaxScore:" + hits.getMaxScore());
                System.out.println("hits========>>");
                for (SearchHit hit : hits) {
                    //?????????????????????????????????
                    System.out.println(hit.getSourceAsString());
                }
                System.out.println("<<========");
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * ????????????
     */
    public void limitSearch(){
        this.connect(
            client -> {
                // ????????????????????????
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // ????????????????????????
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.matchAllQuery());
                // ????????????
                // ?????????????????????(???????????????????????????)??? from
                sourceBuilder.from(0);
        
                // ????????????????????? size
                sourceBuilder.size(2);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // ????????????
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                        //?????????????????????????????????
                        System.out.println(hit.getSourceAsString());
                    }
                    System.out.println("<<========");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        );
    }

    /**
     * ????????????
     */
    public void sortSearch(){
        this.connect(
            client -> {
                // ????????????????????????
                SearchRequest request = new SearchRequest();
                request.indices("user");
        
                // ????????????????????????
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.matchAllQuery());
                // ??????
                sourceBuilder.sort("age", SortOrder.ASC);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // ????????????
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                    //?????????????????????????????????
                        System.out.println(hit.getSourceAsString());
                    }
                    System.out.println("<<========");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        );      
    }

    /**
     * ????????????
     */
    public void combinationSearch(){
        this.connect(
            client -> {
                // ????????????????????????
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // ????????????????????????
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                // ????????????
                boolQueryBuilder.must(QueryBuilders.matchQuery("age", "30"));
                // ????????????
                boolQueryBuilder.mustNot(QueryBuilders.matchQuery("name", "zhangsan"));
                // ????????????
                boolQueryBuilder.should(QueryBuilders.matchQuery("sex", "???"));
                sourceBuilder.query(boolQueryBuilder);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // ????????????
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                for (SearchHit hit : hits) {
                    //?????????????????????????????????
                    System.out.println(hit.getSourceAsString());
                }
                System.out.println("<<========");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
        
            }
        );
    }

    /**
     * ????????????
     */
    public void rangeSearch(){
        this.connect(
            client -> {
                // ????????????????????????
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // ????????????????????????
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age");
                // ????????????
                //rangeQuery.gte("30");
                // ????????????
                rangeQuery.lte("40");
                sourceBuilder.query(rangeQuery);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // ????????????
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                        //?????????????????????????????????
                        System.out.println(hit.getSourceAsString());
                    }
                    System.out.println("<<========");
                } catch (IOException e) {
                    e.printStackTrace();
                }
               
            }
        );  
    }

    /**
     * ????????????
     */
    public void vagueSearch(){
        this.connect(
            client -> {
                // ????????????????????????
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // ????????????????????????
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.fuzzyQuery("name","wangwu").fuzziness(Fuzziness.ONE));
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // ????????????
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                        //?????????????????????????????????
                        System.out.println(hit.getSourceAsString());
                    }
                    System.out.println("<<========");
                } catch (IOException e) {
                    e.printStackTrace();
                }
               
            }
        );
    }

    /**
     * ????????????
     */
    public void highLightSearch(){
        this.connect(
            client -> {
                // ????????????
                SearchRequest request = new SearchRequest().indices("user");
                //2.??????????????????????????????
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                //?????????????????????????????????
                TermsQueryBuilder termsQueryBuilder =
                        QueryBuilders.termsQuery("name","zhangsan");
                //??????????????????
                sourceBuilder.query(termsQueryBuilder);
                //??????????????????
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.preTags("<font color='red'>");//??????????????????
                highlightBuilder.postTags("</font>");//??????????????????
                highlightBuilder.field("name");//??????????????????
                //????????????????????????
                sourceBuilder.highlighter(highlightBuilder);
                //???????????????
                request.source(sourceBuilder);
                //3.??????????????????????????????????????????
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    //4.??????????????????
                    SearchHits hits = response.getHits();
                    System.out.println("took::"+response.getTook());
                    System.out.println("time_out::"+response.isTimedOut());
                    System.out.println("total::"+hits.getTotalHits());
                    System.out.println("max_score::"+hits.getMaxScore());
                    System.out.println("hits::::>>");
                    for (SearchHit hit : hits) {
                        String sourceAsString = hit.getSourceAsString();
                        System.out.println(sourceAsString);
                        //??????????????????
                        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                        System.out.println(highlightFields);
                    }
                    System.out.println("<<::::");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        );
    }

    /**
     * ???????????????
     */
    public void maxValueSearch(){
        this.connect(
            client -> {
                // ????????????
                SearchRequest request = new SearchRequest().indices("user");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.aggregation(AggregationBuilders.max("maxAge").field("age"));
                //???????????????
                request.source(sourceBuilder);
                //3.??????????????????????????????????????????
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    //4.??????????????????
                    SearchHits hits = response.getHits();
                    System.out.println(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        );
    }

    /**
     * ????????????
     */
    public void groupSearch(){
        this.connect(
            client -> {
                SearchRequest request = new SearchRequest().indices("user");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.aggregation(AggregationBuilders.terms("age_groupby").field("age"));
                //???????????????
                request.source(sourceBuilder);
                //3.??????????????????????????????????????????
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                     //4.??????????????????
                    SearchHits hits = response.getHits();
                    System.out.println(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
               
            }
        );
    }

    class User {
        private String name;
        private Integer age;
        private String sex;
        
        public void setName(String name){
            this.name = name;
        }
    
        public void setAge(Integer age){
            this.age = age;
        }
    
        public void setSex(String sex){
            this.sex = sex;
        }
    
        public String getName(){
            return this.name;
        }
    
        public Integer getAge(){
            return this.age;
        }
    
        public String getSex(){
            return this.sex;
        }
    }
    

    /**
     * ????????????elasticsearch?????????????????????????????????????????????????????????es??????
     * ????????????lambda??????connect?????????????????????ElasticsearchTask????????????
     */
    interface ElasticsearchTask{
        void doSomething(RestHighLevelClient client);
    }

}


/**
 * <dependency>
        <groupId>org.elasticsearch</groupId>
        <artifactId>elasticsearch</artifactId>
        <version>7.8.0</version>
    </dependency>
    <!-- elasticsearch ???????????? -->
    <dependency>
        <groupId>org.elasticsearch.client</groupId>
        <artifactId>elasticsearch-rest-high-level-client</artifactId>
        <version>7.8.0</version>
    </dependency>
     <dependency>
        <groupId>com.fasterxml.jackson.core</groupId>
        <artifactId>jackson-databind</artifactId>
        <version>2.9.10.3</version>
    </dependency>
 */