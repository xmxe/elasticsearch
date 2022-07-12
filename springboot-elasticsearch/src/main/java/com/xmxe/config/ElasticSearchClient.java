package com.xmxe.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
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
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class ElasticSearchClient {

    private static final Logger log = LoggerFactory.getLogger(ElasticSearchClient.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private RestHighLevelClient client;


    /**
     * 查询全部索引
     * @return
     */
    public Set<String> getAlias(){
        try {
            GetAliasesRequest request = new GetAliasesRequest();
            GetAliasesResponse response =  client.indices().getAlias(request, RequestOptions.DEFAULT);
            return response.getAliases().keySet();
        } catch (IOException e) {
            log.error("向es发起查询全部索引信息请求失败", e);
        }
        return Collections.emptySet();
    }

    /**
     * 检查索引是否存在
     * @param indexName
     * @return
     */
    public boolean existsIndex(String indexName){
        try {
            // 创建请求
            GetIndexRequest request = new GetIndexRequest().indices(indexName);
            // 执行请求,获取响应
            boolean response = client.indices().exists(request, RequestOptions.DEFAULT);
            return response;
        } catch (Exception e) {
            log.error("向es发起查询索引是否存在请求失败，请求参数：" + indexName, e);
        }
        return false;
    }


    /**
     * 查询索引
     * @param indexName
     * @return
     */
    public String getIndex(String indexName){
        try {
            // 创建请求
            GetIndexRequest request = new GetIndexRequest().indices(indexName);
            // 执行请求,获取响应
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            return response.toString();
        } catch (Exception e) {
            log.error("向es发起查询索引请求失败，请求参数：" + indexName, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * 创建索引
     * @param indexName
     * @param mapping
     * @return
     */
    public void createIndex(String indexName, Map<String, Object> mapping){
        try {
            CreateIndexRequest request = new CreateIndexRequest();
            //索引名称
            request.index(indexName);
            //索引配置
            Settings settings = Settings.builder()
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 1)
                    .put("index.max_inner_result_window", 5000)
                    .build();
            request.settings(settings);
            //索引结构
            request.mapping("_doc",mapping);
            //执行请求,获取响应
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            if(!response.isAcknowledged()){
                throw new RuntimeException("向es发起创建索引请求失败");
            }
            log.info("向es发起创建索引请求成功，返回参数：{}", response.index());
        } catch (Exception e) {
            log.error("向es发起创建索引请求失败，请求参数：" + indexName, e);
            throw new RuntimeException("向es发起创建索引请求失败");
        }
    }


    /**
     * 删除索引
     * @param indexName
     * @return
     */
    public void deleteIndex(String indexName){
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            if(!response.isAcknowledged()){
                throw new RuntimeException("向es发起删除索引请求失败");
            }
            log.info("向es发起删除索引请求成功，请求参数：{}", indexName);
        } catch (Exception e) {
            log.error("向es发起删除索引请求失败，请求参数：" + indexName, e);
            throw new RuntimeException("向es发起删除索引请求失败");
        }
    }


    /**
     * 查询索引映射字段
     * @param indexName
     * @return
     */
    public String getMapping(String indexName){
        try {
            GetMappingsRequest request = new GetMappingsRequest().indices(indexName).types("_doc");
            GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);
            return response.toString();
        } catch (Exception e) {
            log.error("向es发起查询索引映射字段请求失败，请求参数：" + indexName, e);
        }
        return StringUtils.EMPTY;
    }


    /**
     * 添加索引映射字段
     * @param indexName
     * @return
     */
    public void addMapping(String indexName, Map<String, Object> mapping){
        try {
            PutMappingRequest request = new PutMappingRequest();
            request.indices(indexName);
            request.type("_doc");
            //添加字段
            request.source(mapping);
            AcknowledgedResponse response = client.indices().putMapping(request, RequestOptions.DEFAULT);
            if(!response.isAcknowledged()){
                throw new RuntimeException("向es发起添加索引映射字段请求失败");
            }
            log.info("向es发起添加索引映射字段请求成功，请求参数：{}", toJson(request));
        } catch (Exception e) {
            log.error("向es发起添加索引映射字段请求失败，请求参数：" + indexName, e);
            throw new RuntimeException("向es发起添加索引映射字段请求失败");
        }
    }


    /**
     * 向索引中添加文档
     * @param indexName
     * @param id
     * @param obj
     */
    public void addDocument(String indexName, String id, Object obj){
        try {
            //向索引中添加文档
            IndexRequest request = new IndexRequest();
            // 外层参数
            request.id(id);
            request.index(indexName);
            request.type("_doc");

            // 存入对象
            request.source(toJson(obj), XContentType.JSON);
            // 发送请求
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            if(response.status().getStatus() >= 400){
                log.warn("向es发起添加文档数据请求失败，请求参数：{}，返回参数：{}", request.toString(), response.toString());
                throw new RuntimeException("向es发起添加文档数据请求失败");
            }
        } catch (Exception e) {
            log.error("向es发起添加文档数据请求失败，请求参数：" + indexName, e);
            throw new RuntimeException("向es发起添加文档数据请求失败");
        }
    }

    /**
     * 修改索引中的文档数据
     * @param indexName
     * @param id
     * @param obj
     */
    public void updateDocument(String indexName, String id, Map<String,Object> obj){
        try {
            //修改索引中的文档数据
            UpdateRequest request = new UpdateRequest();
            // 外层参数
            request.id(id);
            request.index(indexName);
            request.type("_doc");
            // 存入对象
            request.doc(obj);
            request.doc(toJson(obj), XContentType.JSON);
            // 发送请求
            UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
            if(response.status().getStatus() >= 400){
                log.warn("向es发起修改文档数据请求失败，请求参数：{}，返回参数：{}", request.toString(), response.toString());
                throw new RuntimeException("向es发起修改文档数据请求失败");
            }
        } catch (Exception e) {
            log.error("向es发起修改文档数据请求失败，请求参数：" + indexName, e);
            throw new RuntimeException("向es发起修改文档数据请求失败");
        }
    }



    /**
     * 删除索引中的文档数据
     * @param indexName
     * @param id
     */
    public void deleteDocument(String indexName, String id){
        try {
            //删除索引中的文档数据
            DeleteRequest request = new DeleteRequest();
            // 外层参数
            request.id(id);
            request.index(indexName);
            request.type("_doc");
            // 发送请求
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            if(response.status().getStatus() >= 400){
                log.warn("向es发起删除文档数据请求失败，请求参数：{}，返回参数：{}", request.toString(), response.toString());
                throw new RuntimeException("向es发起删除文档数据请求失败");
            }
        } catch (Exception e) {
            log.error("向es发起删除文档数据请求失败，请求参数：" + indexName, e);
            throw new RuntimeException("向es发起删除文档数据请求失败");
        }
    }


    /**
     * 查询索引中的文档数据
     * @param indexName
     * @param id
     */
    public String getDocumentById(String indexName, String id){
        try {
            GetRequest request = new GetRequest();
            // 外层参数
            request.id(id);
            request.index(indexName);
            request.type("_doc");
            // 发送请求
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            response.getSourceAsString();
        } catch (Exception e) {
            log.error("向es发起查询文档数据请求失败，请求参数：" + indexName, e);
        }
        return StringUtils.EMPTY;
    }


    /**
     * 索引高级查询
     * @param indexName
     * @param source
     * @return
     */
    public SearchResponse searchDocument(String indexName, SearchSourceBuilder source){
        //搜索
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(source);
        try {
            // 执行请求
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            return response;
        } catch (Exception e) {
            log.warn("向es发起查询文档数据请求失败，请求参数：" + searchRequest.toString(), e);
        }
        return null;
    }


    /**
     * 将对象格式化成json，并保持原字段类型输出
     * @param object
     * @return
     */
    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
