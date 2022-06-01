package com.xmxe;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.ImmutableBiMap;

import org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.GetAliasesResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.cluster.metadata.AliasMetaData;
import org.elasticsearch.common.settings.Settings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElasticSearchApplication.class)
/**
 * 创建索引
 */
public class IndexJunit {


    @Autowired
    private RestHighLevelClient client;

    /**
     * 创建索引（简单模式）
     * @throws IOException
     */
    @Test
    public void createIndex() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest("cs_index");
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }


    /**
     * 创建索引（复杂模式）
     * 可以直接把对应的文档结构也一并初始化
     * @throws IOException
     */
    @Test
    public void createIndexComplete() throws IOException {
        CreateIndexRequest request = new CreateIndexRequest();
        //索引名称
        request.index("cs_index");
        //索引配置
        Settings settings = Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 1)
                .build();
        request.settings(settings);

        //映射结构字段
        Map<String, Object> properties = new HashMap<>();
        properties.put("id", ImmutableBiMap.of("type", "text"));
        properties.put("name", ImmutableBiMap.of("type", "text"));
        properties.put("sex", ImmutableBiMap.of("type", "text"));
        properties.put("age", ImmutableBiMap.of("type", "long"));
        properties.put("city", ImmutableBiMap.of("type", "text"));
        properties.put("createTime", ImmutableBiMap.of("type", "long"));
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        //添加一个默认类型
        System.out.println(JSON.toJSONString(request));
        request.mapping("_doc",mapping);
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }


    /**
     * 删除索引
     * @throws IOException
     */
    @Test
    public void deleteIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("cs_index1");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    /**
     * 查询索引
     * @throws IOException
     */
    @Test
    public void getIndex() throws IOException {
        // 创建请求
        GetIndexRequest request = new GetIndexRequest();
        request.indices("cs_index");
        // 执行请求,获取响应
        GetIndexResponse response = client.indices().get(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

    /**
     * 检查索引是否存在
     * @throws IOException
     */
    @Test
    public void exists() throws IOException {
        // 创建请求
        GetIndexRequest request = new GetIndexRequest();
        request.indices("cs_index");
        // 执行请求,获取响应
        boolean response = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

     /**
     * 查询所有的索引名称
     * @throws IOException
     */
    @Test
    public void getAllIndices() throws IOException {
        GetAliasesRequest request = new GetAliasesRequest();
        GetAliasesResponse response =  client.indices().getAlias(request,RequestOptions.DEFAULT);
        Map<String, Set<AliasMetaData>> map = response.getAliases();
        Set<String> indices = map.keySet();
        for (String key : indices) {
            System.out.println(key);
        }
    }


     /**
     * 查询索引映射字段
     * @throws IOException
     */
    @Test
    public void getMapping() throws IOException {
        GetMappingsRequest request = new GetMappingsRequest();
        request.indices("cs_index");
        request.types("_doc");
        GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

     /**
     * 添加索引映射字段
     * @throws IOException
     */
    @Test
    public void addMapping() throws IOException {
        PutMappingRequest request = new PutMappingRequest();
        request.indices("cs_index");
        request.type("_doc");

        //添加字段
        Map<String, Object> properties = new HashMap<>();
        properties.put("accountName", ImmutableBiMap.of("type", "keyword"));
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        request.source(mapping);
        PutMappingResponse response = client.indices().putMapping(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

}