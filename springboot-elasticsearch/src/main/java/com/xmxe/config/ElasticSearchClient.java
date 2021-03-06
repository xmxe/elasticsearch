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
     * ??????????????????
     * @return
     */
    public Set<String> getAlias(){
        try {
            GetAliasesRequest request = new GetAliasesRequest();
            GetAliasesResponse response =  client.indices().getAlias(request, RequestOptions.DEFAULT);
            return response.getAliases().keySet();
        } catch (IOException e) {
            log.error("???es??????????????????????????????????????????", e);
        }
        return Collections.emptySet();
    }

    /**
     * ????????????????????????
     * @param indexName
     * @return
     */
    public boolean existsIndex(String indexName){
        try {
            // ????????????
            GetIndexRequest request = new GetIndexRequest().indices(indexName);
            // ????????????,????????????
            boolean response = client.indices().exists(request, RequestOptions.DEFAULT);
            return response;
        } catch (Exception e) {
            log.error("???es????????????????????????????????????????????????????????????" + indexName, e);
        }
        return false;
    }


    /**
     * ????????????
     * @param indexName
     * @return
     */
    public String getIndex(String indexName){
        try {
            // ????????????
            GetIndexRequest request = new GetIndexRequest().indices(indexName);
            // ????????????,????????????
            GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
            return response.toString();
        } catch (Exception e) {
            log.error("???es????????????????????????????????????????????????" + indexName, e);
        }
        return StringUtils.EMPTY;
    }

    /**
     * ????????????
     * @param indexName
     * @param mapping
     * @return
     */
    public void createIndex(String indexName, Map<String, Object> mapping){
        try {
            CreateIndexRequest request = new CreateIndexRequest();
            //????????????
            request.index(indexName);
            //????????????
            Settings settings = Settings.builder()
                    .put("index.number_of_shards", 3)
                    .put("index.number_of_replicas", 1)
                    .put("index.max_inner_result_window", 5000)
                    .build();
            request.settings(settings);
            //????????????
            request.mapping("_doc",mapping);
            //????????????,????????????
            CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
            if(!response.isAcknowledged()){
                throw new RuntimeException("???es??????????????????????????????");
            }
            log.info("???es????????????????????????????????????????????????{}", response.index());
        } catch (Exception e) {
            log.error("???es????????????????????????????????????????????????" + indexName, e);
            throw new RuntimeException("???es??????????????????????????????");
        }
    }


    /**
     * ????????????
     * @param indexName
     * @return
     */
    public void deleteIndex(String indexName){
        try {
            DeleteIndexRequest request = new DeleteIndexRequest(indexName);
            AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
            if(!response.isAcknowledged()){
                throw new RuntimeException("???es??????????????????????????????");
            }
            log.info("???es????????????????????????????????????????????????{}", indexName);
        } catch (Exception e) {
            log.error("???es????????????????????????????????????????????????" + indexName, e);
            throw new RuntimeException("???es??????????????????????????????");
        }
    }


    /**
     * ????????????????????????
     * @param indexName
     * @return
     */
    public String getMapping(String indexName){
        try {
            GetMappingsRequest request = new GetMappingsRequest().indices(indexName).types("_doc");
            GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);
            return response.toString();
        } catch (Exception e) {
            log.error("???es????????????????????????????????????????????????????????????" + indexName, e);
        }
        return StringUtils.EMPTY;
    }


    /**
     * ????????????????????????
     * @param indexName
     * @return
     */
    public void addMapping(String indexName, Map<String, Object> mapping){
        try {
            PutMappingRequest request = new PutMappingRequest();
            request.indices(indexName);
            request.type("_doc");
            //????????????
            request.source(mapping);
            AcknowledgedResponse response = client.indices().putMapping(request, RequestOptions.DEFAULT);
            if(!response.isAcknowledged()){
                throw new RuntimeException("???es??????????????????????????????????????????");
            }
            log.info("???es????????????????????????????????????????????????????????????{}", toJson(request));
        } catch (Exception e) {
            log.error("???es????????????????????????????????????????????????????????????" + indexName, e);
            throw new RuntimeException("???es??????????????????????????????????????????");
        }
    }


    /**
     * ????????????????????????
     * @param indexName
     * @param id
     * @param obj
     */
    public void addDocument(String indexName, String id, Object obj){
        try {
            //????????????????????????
            IndexRequest request = new IndexRequest();
            // ????????????
            request.id(id);
            request.index(indexName);
            request.type("_doc");

            // ????????????
            request.source(toJson(obj), XContentType.JSON);
            // ????????????
            IndexResponse response = client.index(request, RequestOptions.DEFAULT);
            if(response.status().getStatus() >= 400){
                log.warn("???es??????????????????????????????????????????????????????{}??????????????????{}", request.toString(), response.toString());
                throw new RuntimeException("???es????????????????????????????????????");
            }
        } catch (Exception e) {
            log.error("???es??????????????????????????????????????????????????????" + indexName, e);
            throw new RuntimeException("???es????????????????????????????????????");
        }
    }

    /**
     * ??????????????????????????????
     * @param indexName
     * @param id
     * @param obj
     */
    public void updateDocument(String indexName, String id, Map<String,Object> obj){
        try {
            //??????????????????????????????
            UpdateRequest request = new UpdateRequest();
            // ????????????
            request.id(id);
            request.index(indexName);
            request.type("_doc");
            // ????????????
            request.doc(obj);
            request.doc(toJson(obj), XContentType.JSON);
            // ????????????
            UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
            if(response.status().getStatus() >= 400){
                log.warn("???es??????????????????????????????????????????????????????{}??????????????????{}", request.toString(), response.toString());
                throw new RuntimeException("???es????????????????????????????????????");
            }
        } catch (Exception e) {
            log.error("???es??????????????????????????????????????????????????????" + indexName, e);
            throw new RuntimeException("???es????????????????????????????????????");
        }
    }



    /**
     * ??????????????????????????????
     * @param indexName
     * @param id
     */
    public void deleteDocument(String indexName, String id){
        try {
            //??????????????????????????????
            DeleteRequest request = new DeleteRequest();
            // ????????????
            request.id(id);
            request.index(indexName);
            request.type("_doc");
            // ????????????
            DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
            if(response.status().getStatus() >= 400){
                log.warn("???es??????????????????????????????????????????????????????{}??????????????????{}", request.toString(), response.toString());
                throw new RuntimeException("???es????????????????????????????????????");
            }
        } catch (Exception e) {
            log.error("???es??????????????????????????????????????????????????????" + indexName, e);
            throw new RuntimeException("???es????????????????????????????????????");
        }
    }


    /**
     * ??????????????????????????????
     * @param indexName
     * @param id
     */
    public String getDocumentById(String indexName, String id){
        try {
            GetRequest request = new GetRequest();
            // ????????????
            request.id(id);
            request.index(indexName);
            request.type("_doc");
            // ????????????
            GetResponse response = client.get(request, RequestOptions.DEFAULT);
            response.getSourceAsString();
        } catch (Exception e) {
            log.error("???es??????????????????????????????????????????????????????" + indexName, e);
        }
        return StringUtils.EMPTY;
    }


    /**
     * ??????????????????
     * @param indexName
     * @param source
     * @return
     */
    public SearchResponse searchDocument(String indexName, SearchSourceBuilder source){
        //??????
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName);
        searchRequest.source(source);
        try {
            // ????????????
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            return response;
        } catch (Exception e) {
            log.warn("???es??????????????????????????????????????????????????????" + searchRequest.toString(), e);
        }
        return null;
    }


    /**
     * ?????????????????????json?????????????????????????????????
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
