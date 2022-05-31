package com.xmxe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.xmxe.config.entity.UserDocument;

import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = ElasticSearchApplication.class)
/**
 * 操作文档
 */
public class DocJunit {

    @Autowired
    private RestHighLevelClient client;


    /**
     * 添加文档
     * @throws IOException
     */
    @Test
    public void addDocument() throws IOException {
        // 创建对象
        UserDocument user = new UserDocument();
        user.setId("1");
        user.setName("里斯");
        user.setCity("武汉");
        user.setSex("男");
        user.setAge(20);
        user.setCreateTime(new Date());

        // 创建索引,即获取索引
        IndexRequest request = new IndexRequest();
        // 外层参数
        request.id("1");
        request.index("cs_index");
        request.type("_doc");
        request.timeout(TimeValue.timeValueSeconds(1));
        // 存入对象
        request.source(JSON.toJSONString(user), XContentType.JSON);
        // 发送请求
        System.out.println(request.toString());
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

     /**
     * 更新文档（按需修改）
     * @throws IOException
     */
    @Test
    public void updateDocument() throws IOException {
        // 创建对象
        UserDocument user = new UserDocument();
        user.setId("2");
        user.setName("程咬金");
        user.setCreateTime(new Date());
        // 创建索引,即获取索引
        UpdateRequest request = new UpdateRequest();
        // 外层参数
        request.id("2");
        request.index("cs_index");
        request.type("_doc");
        request.timeout(TimeValue.timeValueSeconds(1));
        // 存入对象
        request.doc(JSON.toJSONString(user), XContentType.JSON);
        // 发送请求
        System.out.println(request.toString());
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }


    /**
     * 删除文档
     * @throws IOException
     */
    @Test
    public void deleteDocument() throws IOException {
        // 创建索引,即获取索引
        DeleteRequest request = new DeleteRequest();
        // 外层参数
        request.id("1");
        request.index("cs_index");
        request.type("_doc");
        request.timeout(TimeValue.timeValueSeconds(1));
        // 发送请求
        System.out.println(request.toString());
        DeleteResponse response = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

     /**
     * 查询文档是不是存在
     * @throws IOException
     */
    @Test
    public void exists() throws IOException {
        // 创建索引,即获取索引
        GetRequest request = new GetRequest();
        // 外层参数
        request.id("3");
        request.index("cs_index");
        request.type("_doc");
        // 发送请求
        System.out.println(request.toString());
        boolean response = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    /**
     * 通过ID，查询指定文档
     * @throws IOException
     */
    @Test
    public void getById() throws IOException {
        // 创建索引,即获取索引
        GetRequest request = new GetRequest();
        // 外层参数
        request.id("1");
        request.index("cs_index");
        request.type("_doc");
        // 发送请求
        System.out.println(request.toString());
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        System.out.println(response.toString());
    }

      /**
     * 批量添加文档
     * @throws IOException
     */
    @Test
    public void batchAddDocument() throws IOException {
        // 批量请求
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(10));
        // 创建对象
        List<UserDocument> userArrayList = new ArrayList<>();
        Date date = new Date();
        userArrayList.add(new UserDocument("1","张三", "男", 30, "武汉",date));
        userArrayList.add(new UserDocument("2","里斯", "女", 31, "北京",date));
        userArrayList.add(new UserDocument("3","王五", "男", 32, "武汉",date));
        userArrayList.add(new UserDocument("4","赵六", "女", 33, "长沙",date));
        userArrayList.add(new UserDocument("5","七七", "男", 34, "武汉",date));
        // 添加请求
        for (int i = 0; i < userArrayList.size(); i++) {
            userArrayList.get(i).setId(String.valueOf(i));
            IndexRequest indexRequest = new IndexRequest();
            // 外层参数
            indexRequest.id(String.valueOf(i));
            indexRequest.index("cs_index");
            indexRequest.type("_doc");
            indexRequest.timeout(TimeValue.timeValueSeconds(1));
            indexRequest.source(JSON.toJSONString(userArrayList.get(i)), XContentType.JSON);
            bulkRequest.add(indexRequest);
        }
        // 执行请求
        BulkResponse response = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(response.status());
    }


}