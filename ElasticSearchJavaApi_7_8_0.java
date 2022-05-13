
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
     * 创建客户端
     */
    public void helloElasticsearch() throws Exception{
        // 创建客户端对象
		RestHighLevelClient client = new RestHighLevelClient(
            RestClient.builder(new HttpHost("localhost", 9200, "http")));
        //		...
        System.out.println(client);

        // 关闭客户端连接
        client.close();
    }

    // ---------------索引--------------

    /**
     * 创建索引
     */
    public void createIndex() throws Exception{
        // 创建客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        // 创建索引 - 请求对象
        CreateIndexRequest request = new CreateIndexRequest("user2");
        // 发送请求，获取响应
        CreateIndexResponse response = client.indices().create(request,
                RequestOptions.DEFAULT);
        boolean acknowledged = response.isAcknowledged();
        // 响应状态
        System.out.println("操作状态 = " + acknowledged);

        // 关闭客户端连接
        client.close();
    }

    /**
     * 查询索引
     */
    public void searchIndex()throws Exception{
        // 创建客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

        // 查询索引 - 请求对象
        GetIndexRequest request = new GetIndexRequest("user2");
        // 发送请求，获取响应
        GetIndexResponse response = client.indices().get(request,
                RequestOptions.DEFAULT);
        
        System.out.println("aliases:"+response.getAliases());
        System.out.println("mappings:"+response.getMappings());
        System.out.println("settings:"+response.getSettings());

        client.close();
    }

    /**
     * 删除索引
     */
    public void deleteIndex()throws Exception{
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
        // 删除索引 - 请求对象
        DeleteIndexRequest request = new DeleteIndexRequest("user2");
        // 发送请求，获取响应
        AcknowledgedResponse response = client.indices().delete(request,RequestOptions.DEFAULT);
        // 操作结果
        System.out.println("操作结果 ： " + response.isAcknowledged());
        client.close();
    }


    // --------文档---------

    /**
     * 重构连接操作
     * @param task 连接后操作es的具体逻辑s
     */
    public void connect(ElasticsearchTask task){
        // 创建客户端对象
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));
        try {
            task.doSomething(client);
            // 关闭客户端连接
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 新增文档
     */
    public void insertDoc(){
        this.connect(client -> {
            // 新增文档 - 请求对象
            IndexRequest request = new IndexRequest();
            // 设置索引及唯一性标识
            request.index("user").id("1001");

            // 创建数据对象
            User user = new User();
            user.setName("zhangsan");
            user.setAge(30);
            user.setSex("男");
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                String productJson = objectMapper.writeValueAsString(user);
               
                // 添加文档数据，数据格式为 JSON 格式
                request.source(productJson, XContentType.JSON);
                // 客户端发送请求，获取响应对象
                IndexResponse response = client.index(request, RequestOptions.DEFAULT);
                // 打印结果信息
                System.out.println("_index:" + response.getIndex());
                System.out.println("_id:" + response.getId());
                System.out.println("_result:" + response.getResult());

            } catch (Exception e) {
                e.printStackTrace();
            }
            
        });
    }


    /**
     * 修改文档
     */
    public void updateDoc(){
        this.connect(client -> {
            // 修改文档 - 请求对象
            UpdateRequest request = new UpdateRequest();
            // 配置修改参数
            request.index("user").id("1001");
            // 设置请求体，对数据进行修改
            request.doc(XContentType.JSON, "sex", "女");
            // 客户端发送请求，获取响应对象
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
     * 查询文档
     */
    public void getDoc(){
        this.connect(client -> {
            //1.创建请求对象
            GetRequest request = new GetRequest().index("user").id("1001");
            //2.客户端发送请求，获取响应对象
            GetResponse response;
            try {
                response = client.get(request, RequestOptions.DEFAULT);
                // 3.打印结果信息
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
     * 删除文档
     */
    public void deleteDOc(){
        this.connect(client -> {
            //创建请求对象
            DeleteRequest request = new DeleteRequest().index("user").id("1001");
            //客户端发送请求，获取响应对象
            DeleteResponse response;
            try {
                response = client.delete(request, RequestOptions.DEFAULT);
                 //打印信息
                System.out.println(response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        });
    }

    /**
     * 批量新增文档
     */
    public void batchInsertDoc(){
        this.connect(client -> {
            //创建批量新增请求对象
            BulkRequest request = new BulkRequest();
            request.add(new IndexRequest().index("user").id("1001").source(XContentType.JSON, "name", "zhangsan"));
            request.add(new IndexRequest().index("user").id("1002").source(XContentType.JSON, "name", "lisi"));
            request.add(new IndexRequest().index("user").id("1003").source(XContentType.JSON, "name", "wangwu"));
            //客户端发送请求，获取响应对象
            BulkResponse responses;
            try {
                responses = client.bulk(request, RequestOptions.DEFAULT);
                //打印结果信息
                System.out.println("took:" + responses.getTook());
                System.out.println("items:" + responses.getItems());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        });
    }


    /**
     * 批量删除文档
     */
    public void batchDeleteDoc(){
        this.connect(client -> {
            //创建批量删除请求对象
            BulkRequest request = new BulkRequest();
            request.add(new DeleteRequest().index("user").id("1001"));
            request.add(new DeleteRequest().index("user").id("1002"));
            request.add(new DeleteRequest().index("user").id("1003"));
            //客户端发送请求，获取响应对象
            BulkResponse responses;
            try {
                responses = client.bulk(request, RequestOptions.DEFAULT);
                //打印结果信息
                System.out.println("took:" + responses.getTook());
                System.out.println("items:" + responses.getItems());
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        });
    }


    /**
     * 查询索引所有数据
     */
    public void queryDoc(){
        this.connect(client -> {
            // 创建搜索请求对象
            SearchRequest request = new SearchRequest();
            request.indices("user");
            // 构建查询的请求体
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            // 查询所有数据
            sourceBuilder.query(QueryBuilders.matchAllQuery());
            request.source(sourceBuilder);
            SearchResponse response;
            try {
                response = client.search(request, RequestOptions.DEFAULT);
                // 查询匹配
                SearchHits hits = response.getHits();
                System.out.println("took:" + response.getTook());
                System.out.println("timeout:" + response.isTimedOut());
                System.out.println("total:" + hits.getTotalHits());
                System.out.println("MaxScore:" + hits.getMaxScore());
                System.out.println("hits========>>");
                for (SearchHit hit : hits) {
                //输出每条查询的结果信息
                    System.out.println(hit.getSourceAsString());
                }
                System.out.println("<<========");
            } catch (IOException e) {
                e.printStackTrace();
            }
           
        });
    }
    
    /**
     * 条件查询
     */
    public void conditionSearch(){
        this.connect(client -> {
            // 创建搜索请求对象
            SearchRequest request = new SearchRequest();
            request.indices("user");
            // 构建查询的请求体
            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
            sourceBuilder.query(QueryBuilders.termQuery("age", "30"));
            request.source(sourceBuilder);
            SearchResponse response;
            try {
                response = client.search(request, RequestOptions.DEFAULT);
                // 查询匹配
                SearchHits hits = response.getHits();
                System.out.println("took:" + response.getTook());
                System.out.println("timeout:" + response.isTimedOut());
                System.out.println("total:" + hits.getTotalHits());
                System.out.println("MaxScore:" + hits.getMaxScore());
                System.out.println("hits========>>");
                for (SearchHit hit : hits) {
                    //输出每条查询的结果信息
                    System.out.println(hit.getSourceAsString());
                }
                System.out.println("<<========");
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * 分页查询
     */
    public void limitSearch(){
        this.connect(
            client -> {
                // 创建搜索请求对象
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // 构建查询的请求体
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.matchAllQuery());
                // 分页查询
                // 当前页其实索引(第一条数据的顺序号)， from
                sourceBuilder.from(0);
        
                // 每页显示多少条 size
                sourceBuilder.size(2);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // 查询匹配
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                        //输出每条查询的结果信息
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
     * 查询排序
     */
    public void sortSearch(){
        this.connect(
            client -> {
                // 创建搜索请求对象
                SearchRequest request = new SearchRequest();
                request.indices("user");
        
                // 构建查询的请求体
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.matchAllQuery());
                // 排序
                sourceBuilder.sort("age", SortOrder.ASC);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // 查询匹配
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                    //输出每条查询的结果信息
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
     * 组合查询
     */
    public void combinationSearch(){
        this.connect(
            client -> {
                // 创建搜索请求对象
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // 构建查询的请求体
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
                // 必须包含
                boolQueryBuilder.must(QueryBuilders.matchQuery("age", "30"));
                // 一定不含
                boolQueryBuilder.mustNot(QueryBuilders.matchQuery("name", "zhangsan"));
                // 可能包含
                boolQueryBuilder.should(QueryBuilders.matchQuery("sex", "男"));
                sourceBuilder.query(boolQueryBuilder);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // 查询匹配
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                for (SearchHit hit : hits) {
                    //输出每条查询的结果信息
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
     * 范围查询
     */
    public void rangeSearch(){
        this.connect(
            client -> {
                // 创建搜索请求对象
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // 构建查询的请求体
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("age");
                // 大于等于
                //rangeQuery.gte("30");
                // 小于等于
                rangeQuery.lte("40");
                sourceBuilder.query(rangeQuery);
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // 查询匹配
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                        //输出每条查询的结果信息
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
     * 模糊查询
     */
    public void vagueSearch(){
        this.connect(
            client -> {
                // 创建搜索请求对象
                SearchRequest request = new SearchRequest();
                request.indices("user");
                // 构建查询的请求体
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.query(QueryBuilders.fuzzyQuery("name","wangwu").fuzziness(Fuzziness.ONE));
                request.source(sourceBuilder);
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    // 查询匹配
                    SearchHits hits = response.getHits();
                    System.out.println("took:" + response.getTook());
                    System.out.println("timeout:" + response.isTimedOut());
                    System.out.println("total:" + hits.getTotalHits());
                    System.out.println("MaxScore:" + hits.getMaxScore());
                    System.out.println("hits========>>");
                    for (SearchHit hit : hits) {
                        //输出每条查询的结果信息
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
     * 高亮查询
     */
    public void highLightSearch(){
        this.connect(
            client -> {
                // 高亮查询
                SearchRequest request = new SearchRequest().indices("user");
                //2.创建查询请求体构建器
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                //构建查询方式：高亮查询
                TermsQueryBuilder termsQueryBuilder =
                        QueryBuilders.termsQuery("name","zhangsan");
                //设置查询方式
                sourceBuilder.query(termsQueryBuilder);
                //构建高亮字段
                HighlightBuilder highlightBuilder = new HighlightBuilder();
                highlightBuilder.preTags("<font color='red'>");//设置标签前缀
                highlightBuilder.postTags("</font>");//设置标签后缀
                highlightBuilder.field("name");//设置高亮字段
                //设置高亮构建对象
                sourceBuilder.highlighter(highlightBuilder);
                //设置请求体
                request.source(sourceBuilder);
                //3.客户端发送请求，获取响应对象
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    //4.打印响应结果
                    SearchHits hits = response.getHits();
                    System.out.println("took::"+response.getTook());
                    System.out.println("time_out::"+response.isTimedOut());
                    System.out.println("total::"+hits.getTotalHits());
                    System.out.println("max_score::"+hits.getMaxScore());
                    System.out.println("hits::::>>");
                    for (SearchHit hit : hits) {
                        String sourceAsString = hit.getSourceAsString();
                        System.out.println(sourceAsString);
                        //打印高亮结果
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
     * 最大值查询
     */
    public void maxValueSearch(){
        this.connect(
            client -> {
                // 高亮查询
                SearchRequest request = new SearchRequest().indices("user");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.aggregation(AggregationBuilders.max("maxAge").field("age"));
                //设置请求体
                request.source(sourceBuilder);
                //3.客户端发送请求，获取响应对象
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                    //4.打印响应结果
                    SearchHits hits = response.getHits();
                    System.out.println(response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
            }
        );
    }

    /**
     * 分组查询
     */
    public void groupSearch(){
        this.connect(
            client -> {
                SearchRequest request = new SearchRequest().indices("user");
                SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
                sourceBuilder.aggregation(AggregationBuilders.terms("age_groupby").field("age"));
                //设置请求体
                request.source(sourceBuilder);
                //3.客户端发送请求，获取响应对象
                SearchResponse response;
                try {
                    response = client.search(request, RequestOptions.DEFAULT);
                     //4.打印响应结果
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
     * 定义一个elasticsearch的操作接口，通过实现这个接口进行具体的es操作
     * 直接使用lambda调用connect方法，参数就是ElasticsearchTask的实现类
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
    <!-- elasticsearch 的客户端 -->
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