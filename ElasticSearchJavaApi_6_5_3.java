import java.net.InetAddress;
import java.util.Map;

import com.alibaba.fastjson.JSON;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;

public class ElasticSearchJavaApi_6_5_3{
  
  private TransportClient client;
  /**
   * 创建客户端
   */
  public void createClient(){
    Settings settings = Settings.builder().put("cluster.name", "bigdata").build();
    TransportClient client = new PreBuiltTransportClient(settings);
  
    //用来指定集群中的节点,TCP/IP协议，es服务器的端口号是：9300； HTTP协议，端口号是9200
    TransportAddress janson01 = new TransportAddress(InetAddress.getByName("JANSON01"), 9300);
    TransportAddress janson02 = new TransportAddress(InetAddress.getByName("JANSON02"), 9300);
    TransportAddress janson03 = new TransportAddress(InetAddress.getByName("JANSON03"), 9300);
  
    client.addTransportAddresses(janson01, janson02, janson03);
    System.out.println(client);

    /**
     * 测试JAVA 客户端能否正常查询指定索引库中的信息
     */

    //需求：查询索引库bigdata中type之product,id为1的document信息。
    GetResponse response = client.prepareGet("bigdata", "product", "2").get();
    System.out.println("查询到的结果是：" + response.getSourceAsString());

     /**
     * 资源释放
     */
    if (client != null) {
      client.close();
    }

  }


  public void curd(){
    /**
     * 测试新增索引信息
     */
    //需求：向索引库bigdata中type之product中新增一条document,该document的信息如下：{"name":"storm","author":"Apache storm team","version":"2.3.5"}

    //使用FastJson将实例转换成json格式的数据
    Product product = new Product("storm", "Apache storm team", "2.3.5");

    String jsonStr = JSON.toJSONString(product);

    IndexResponse indexResponse = client.prepareIndex("bigdata", "product")
            .setSource(jsonStr, XContentType.JSON)
            .get();

    System.out.println("待新增的document的信息是：" + jsonStr + "，新增操作后，获得了来自远程es集群反馈的信息是：" + indexResponse);

    /**
     * 测试删除索引信息
     */
     //需求：从索引库bigdata中type之product中，删除一条id为IxVruWkBtUUytMiZ4rxw的document。
    DeleteResponse delResponse = client.prepareDelete("bigdata", "product", "IxVruWkBtUUytMiZ4rxw").get();
    String finalResult = "deleted".equals(delResponse.getResult().toString().toLowerCase()) ? "删除成功！" : "删除失败！555...";
    System.out.println("删除成功否？" + finalResult);

    /**
     * 测试修改
     */
    //需求：针对索引库bigdata中type之product，将id为OiUhuGkBJFmjDtb2b5p9的document的name更新为“独孤求败”,version更新为1.6.5。 （局部更新）

    UpdateResponse updateResponse = client.prepareUpdate("bigdata", "product", "OiUhuGkBJFmjDtb2b5p9").setDoc("name", "独孤求败", "version", "1.6.5").get();

    System.out.println("获得了来自远程es集群的反馈信息是："+updateResponse);
    

    /**
     * 测试批处理操作
     */
    //需求：针对于索引库bigdata中的type之product,进行如下的操作：
    //①新增一条document，内容: ｛“name”:"flume","author":"阿诺德.施瓦辛格","version":"2.6.8"｝
    //②删除一条document，该document的id是：OSUhuGkBJFmjDtb2b5pO
    //③更新一条document，该document的id是：OiUhuGkBJFmjDtb2b5p9，将name更新为：spark, author更新为独孤求败

    BulkResponse bulkResponse = client.prepareBulk()
            .add(new IndexRequest("bigdata", "product").source(JSON.toJSONString(new Product("flume", "阿诺德.施瓦辛格", "2.6.8")), XContentType.JSON))
            .add(new DeleteRequest("bigdata", "product", "OSUhuGkBJFmjDtb2b5pO"))
            .add(new UpdateRequest("bigdata", "product", "OiUhuGkBJFmjDtb2b5p9").doc("name", "spark", "author", "独孤求败"))
            .get();

    //分析结果
    for(BulkItemResponse result:bulkResponse.getItems()){
        String bulkResult = result.isFailed()?"失败":"成功";
        System.out.println("操作成功否？"+bulkResult);
    }


    /**
     * 测试：检索类型，以及分页检索
     */
    //案例1：检索bigdata索引库中，product type中的字段name为hive的索引信息。学习知识点： 检索类型，分页检索
    String[] indices = {"bank", "bigdata"};
    SearchResponse searchResponse = client.prepareSearch(indices)
            //指定所关注的type
            .setTypes("product")
            //设定searchType
            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
            //设置分页（查询第一页，每页显示2条记录）,公式：开始索引 = （页码-1）*pageSize
            .setFrom((1 - 1) * 2)
            .setSize(2)
            //设置查询的条件
            .setQuery(QueryBuilders.termQuery("name", "hive"))
            .get();


    //从结果中显示所有满足条件的记录
    SearchHits hits = searchResponse.getHits();
    for (SearchHit hit : hits) {
        System.out.println("检索到的document信息是：" + hit.getSourceAsString());
    }
    

    /**
     * 测试：被检索的关键字高亮显示
     */
    //案例2：检索bigdata索引库中， type之product中的字段author中包含 storm的索引信息，需要高亮显示(飘红显示)。

    //步骤：
    //①构建HighlightBuilder的实例，该实例中封装了待高亮显示的关键字的前后缀，以及需要检索的字段
    HighlightBuilder builder = new HighlightBuilder();
    builder.field("author")
            .preTags("<font style='color:red;size=35'>")
            .postTags("</font>");

    //②检索
    SearchResponse searchResponse2 = client.prepareSearch(indices)
            //指定所关注的type
            .setTypes("product")
            //设置查询的条件
            .setQuery(QueryBuilders.fuzzyQuery("author", "storm"))
            //设置高亮显示构建器的实例
            .highlighter(builder)
            .get();


    //③分析检索后的结果
    SearchHits hits2 = searchResponse2.getHits();
    for (SearchHit hit : hits2) {
        //logger.info("检索到的document信息是：" + hit.getSourceAsString());

        //核心步骤：
        //a)将当前的documet封装到Map实例中
        Map<String, Object> source = hit.getSourceAsMap();

        //b)替换key对应的值 （key:检索的关键字对应的field） ~>谚语：狸猫换太子
        Map<String, HighlightField> highlightFields = hit.getHighlightFields();
        for(Map.Entry<String, HighlightField> entry:highlightFields.entrySet()){
            String key = entry.getKey();//希望高亮显示的field名
            HighlightField value = entry.getValue(); //value就封装了高亮显示的前后缀
            //System.out.println("key:"+key+",value:"+value);
            source.put(key,value.getFragments()[0].toString());
        }

        //c)显示最终的结果（带了前后缀）
        System.out.println("被检索的关键字高亮显示的结果是："+ JSON.toJSONString(source));
    }
    
    /**
     * 查询索引库test-ok中的type之news,查询字段content中包含“中”的索引信息 （使用默认的分词法）
     */  
    SearchResponse searchResponse3 = client.prepareSearch("test-ok")
            .setTypes("news")
            //设置检索的条件
            .setQuery(QueryBuilders.termQuery("content", "中国"))
            .get();

    //输出查询到的索引信息
    for (SearchHit hit : searchResponse3.getHits()) {
        System.out.println(hit.getSourceAsString());
    }

    /**
     * 查询索引库chinese中的type之hot,查询字段content中包含"中国"的索引信息 （使用ik中文分词）
     */
    SearchResponse searchResponse4 = client.prepareSearch("chinese")
            .setTypes("hot")
            //设置检索的条件
            .setQuery(QueryBuilders.termQuery("content", "中国"))
            .get();

    //输出查询到的索引信息
    for (SearchHit hit : searchResponse4.getHits()) {
        System.out.println(hit.getSourceAsString());
    }

  }
	
}

class Product {
    
    private String name;
    private String author;
    private String version;

    public Product(String name, String author, String version) {
        this.name = name;
        this.author = author;
        this.version = version;
    }

    public void setName(String name){
      this.name = name;
    }
    public String getName(){
      return name;
    }

    public void setAuthor(String author){
      this.author = author;
    }
    public String getAuthor(){
      return author;
    }

    public void setVersion(String version){
      this.version = version;
    }
    public String getVersion(){
      return version;
    }
}
/**
	 <!-- es客户端的依赖，其中包含了一个核心的api: TransportClient -->
    <dependency>
      <groupId>org.elasticsearch.client</groupId>
      <artifactId>transport</artifactId>
      <!--下述通过el表达式读取上述es-version标签标签体的值-->
      <version>${es-version}</version>
    </dependency>

    <!-- es服务器对应的核心依赖 -->
    <dependency>
      <groupId>org.elasticsearch</groupId>
      <artifactId>elasticsearch</artifactId>
      <version>${es-version}</version>
    </dependency>
*/