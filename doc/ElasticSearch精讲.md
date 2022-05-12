### 第Ⅰ讲

#### ES简介之概述
ES，是elasticsearch的缩写，是一款基于Lucene的开源的分布式查询和分析引擎。

#### ES和Solr对比
相同点：都是基于Lucene，都是对lucene的封装。

不同点：
①使用：Solr安装略微复杂一些；es基本是开箱即用，非常简单
②接口：Solr类似webservice的接口；es REST风格的访问接口
③分布式存储：solrCloud  solr4.x才支持，es是为分布式而生的
④支持的格式：Solr 支持更多格式的数据，比如JSON、XML、CSV；es仅支持json文件格式
⑤近实时搜索的角度：Solr 查询快，但更新索引时慢（即插入删除慢），用于电商等查询多的应用；ES建立索引快（即查询慢），即实时性查询快，用于facebook新浪等搜索。Solr 是传统搜索应用的有力解决方案，但 Elasticsearch 更适用于新兴的实时搜索应用。

#### 和MySQL对比

| MySQL            | ES              |
| ---------------- | --------------- |
| database(数据库) | index（索引库） |
| table(表)        | type(类型)      |
| row(行，记录)    | document(文档)  |
| column(列)       | field(字段)     |


#### REST简介
REST全称Representational State Transfer。是一种软件的架构风格，而不是标准，只是提供了一组设计原则和约束条件。它主要用于客户端和服务器交互类的软件。基于这个风格设计的软件可以更简洁，更有层次，更易于实现缓存等机制。其实说白了就是类似HTTP的访问，和HTTP非常的相似。
REST操作：
~~~
GET：获取对象的当前状态；
PUT：改变对象的状态；
POST：创建对象；
DELETE：删除对象；
HEAD：获取头信息。
~~~



### 第Ⅱ讲

#### REST具体操作说明
URI: uniform resources identifier,统一资源标识符。用来定位网络上的资源，可以使用：http,ftp等协议进行定位。
URL: uniform resources locator, 统一资源定位符。选用http协议，如：https://www.baidu.com/img/baidu_jgylogo3.gif

二者的关系：URL是URI的子集，URI包含URL。


#### ES内置REST接口
（注意：此处的接口指的就是url，不是Java语言中的接口）

| 接口 | 说明 |
| ---- | ---- |
|/index/_search|针对具体的索引库进行检索|
|/index|查询或是新增索引|
|/index/type|创建或是操作类型|
|/_aliase|获取或是操作索引的别名|
|/index/_mapping|获取或是操作mapping|
|/index/_setting|获取或是操作设置|
|/index/_open|打开被关闭的索引|
|/index/_close|关闭指定的索引|
|/index/_refresh|刷新指定的索引，目的是保证让新增的索引可见（不保证数据持久化）|
|/index/_flush|刷新索引（触发底层lucene的提交）|



#### ES安装_配置介绍

##### ES单机版安装：

①默认配置版
  步骤：将安装包上传到Linux下，解压，在普通用户下运行elasticSearch/bin/elasticsearch 文件
  注意点：

1. 必须是普通用户，不能是root用户（否则，报错：java.lang.RuntimeException: can not run elasticsearch as root）

2. elasticSearch/bin/elasticsearch -d ~>以后台进程的方式启动es，通过jps命令，可以察觉到进程名为：Elasticsearch

3. Linux命令：useradd 新用户名  ~>新建用户 passwd 用户名 ~>设置密码 su -l 用户名  ~>用户切换

4. curl: linux命令，可以模拟browser向远程的服务器发送请求，并获得反馈。
  curl:linux os中的一个命令，可以使用命令行的方式模拟browser向远程的server发送请求，并获得远程server的反馈
  ip:  联网的终端设别在网络上的唯一标识,端口号：联网的终端设备上安装的具有访问网络功能的应用程序的唯一标识。语法：curl -XGET 'http://127.0.0.1:9200'

  

②手动定制版
配置config/elasticsearch.yml
~~~yaml
cluster.name: bigdata  ~>集群名
node.name: hadoop   ~>集群中当前es服务器节点名
path.data: /home/jerry/data/elastic     ~> es索引库中的数据最终存储到哪个目录下，目录会自动创建
path.logs: /home/jerry/logs/elastic    ~> es进程启动后，对应的日志信息存放的目录，目录会自动创建
network.host: JANSON01    ~> 当前虚拟机的ip地址的别名
http.cors.enabled: true     ~> 下面两个配置参数指的是es服务器允许别的插件服务访问 (插件: 对现有软件功能的一个扩展的软件)
http.cors.allow-origin: "*"
启动：（daemon: 精灵进程，后台进程的方式启动； 索引库启动需要花费几秒中的时间，等待!）
~~~
启动：$ELASTICSEARCH_HOME/bin/elasticsearch -d 
注意：
1，若是进程启动不了，查看日志文件/home/tom/logs/elastic/bigdata.log，报错：
max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]，
解决方案见：Elasticsearch\1_资料\⑥异常\Ⅰ-es安装异常.txt
2，yml,properties:
同：都是用来操作资源文件的。不同点： ①properties资源文件中，键与值之间使用=进行分隔 (等于号) yml资源文件中，键与值之间使用:进行分隔 （冒号后面必须得添加一个半角空格）②较之于properties资源文件中，yml资源文件书写起来更加简洁一些，通过缩进来标识层次关系。



③如何验证上述每种方式安装成功了？
[root@JANSON01 ~]# curl -XGET 'http://127.0.0.1:9200'
~~~json
{
	"name" : "Vo0PTEn",
	"cluster_name" : "elasticsearch",
	"cluster_uuid" : "ZULM4XLsSZi3cIvbbcalfg",
	"version" : {
		"number" : "6.5.3",
		"build_flavor" : "default",
		"build_type" : "tar",
		"build_hash" : "159a78a",
		"build_date" : "2018-12-06T20:11:28.826501Z",
		"build_snapshot" : false,
		"lucene_version" : "7.5.0",
		"minimum_wire_compatibility_version" : "5.6.0",
		"minimum_index_compatibility_version" : "5.0.0"
	},
	"tagline" : "You Know, for Search"
}
~~~

##### ES集群版安装（待续）：
xxx


#### ES配置文件说明
logging.yml:日志配置文件，es也是使用log4j来记录日志的，所以logging.yml里的设置按普通log4j配置来设置就行了。
elasticsearch.yml:es的基本配置文件,需要注意的是key和value的格式“:”之后需要一个空格。
修改如下配置之后，就可以从别的机器上进行访问了,修改node.name,Transport.tcp.port: 9300 设置节点间交互的tcp端口，默认为9300
在Elasticsearch集群中可以监控统计很多信息，但是只有一个是最重要的时集群健康(cluster health)。Es中用三种颜色状态表示:green，yellow，red.Green：所有主分片和副本分片都可用Yellow：所有主分片可用，但不是所有副本分片都可用,Red：不是所有的主分片都可用




### 第Ⅲ讲

#### CURL_简介

CURL: 使用命令行的方式向远程的服务通过http协议发送GET/POST请求，并获得来自远程服务器端的反馈。（类比：使用命令行模拟browser和远程的server进行信息交互）
curl常用的参数有：-X  用来后接请求参数，如：GET POST PUT DELETE，-d 用来标识客户端向远程服务器发送数据，数据的格式一般是json，-H 用来标识请求头的信息

#### CURL_创建索引库和索引 

需求1：使用curl创建一个名为bigdata的索引库。
~~~json
	[jerry@JANSON01 es]$ curl -XPUT 'http://JANSON01:9200/bigdata?pretty'
	{
	  "acknowledged" : true,
	  "shards_acknowledged" : true,
	  "index" : "bigdata"
	}
~~~
需求2：使用curl在名为bigdata的索引库下创建一个名为product的type,且在该type下新增一条索引标识为1的document,该document的信息如下：{"name":"Hadoop","author":"Dok cultting","last_version":"3.0.0"}
实操演示：

~~~json
	[jerry@JANSON01 es]$ curl -XPOST 'http://JANSON01:9200/bigdata/product/1' -H 'Content-Type:application/json' -d 
	{
	"name":"Hadoop",
	"author":"Doc Cultting",
	"last_version":"3.0.1"
	}
{"_index":"bigdata","_type":"product","_id":"1","_version":1,"result":"created","_shards":{"total":2,"successful":1,"failed":0},"_seq_no":0,"_primary_term":1}
~~~

需求3：使用curl在名为bigdata的索引库下创建一个名为product的type,且在该type下新增一条使用默认索引标志的document,该document的信息如下：{"name":"Spark","author":"杰克逊","last_version":"2.4.5"}
实操演示：
~~~json
	[jerry@JANSON01 es]$ curl -XPOST 'http://JANSON01:9200/bigdata/product?pretty' -H 'Content-Type:application/json' -d '{ 
	> "name":"Spark",
	> "author":"杰克逊",
	> "last_version":"2.3.4"
	> }'
		反馈的结果是：
	{
	  "_index" : "bigdata",
	  "_type" : "product",
	  "_id" : "HM9rL2oB300nRXZV6BRB",
	  "_version" : 1,
	  "result" : "created",
	  "_shards" : {
		"total" : 2,
		"successful" : 1,
		"failed" : 0
	  },
	  "_seq_no" : 1,
	  "_primary_term" : 1
	}
~~~



### 第Ⅳ讲

#### 关于Put和Post

##### 全局更新 
一般不使用，效果是：将旧的索引信息全部替换为新的索引信息

使用post put都可以
使用post,将索引标识为1索引信息更新为："name":"HADOOP", "author":"郭富城"
~~~json
curl  -H 'Content-Type: application/json'  -XPOST 'http://JANSON01:9200/bigdata/product/1?pretty'  -d '{"name":"HADOOP", "author":"郭富城"}'
~~~
使用put,将索引标识为1索引信息更新为："name":"hadoop", "author":"道哥.卡廷"
~~~json
curl  -H 'Content-Type: application/json'  -XPUT 'http://JANSON01:9200/bigdata/product/1?pretty'  -d '{"author":"道哥.卡廷"}'
~~~
说明：①但是这些操作都是全局更新，可以理解为先将旧的document删除，然后重新创建一个新的、id相同的document。②PUT是幂等方法，POST不是。所以PUT用于更新，POST用于新增比较合适。（幂等操作：无论进行多少次操作，最终的结果是一致的。如：StringBuffer）③ES创建索引库和索引时的注意点 a)索引库名称必须要全部小写，不能以下划线开头，也不能包含逗号 b)如果没有明确指定索引数据的ID，那么es会自动生成一个随机的ID，需要使用POST参数
~~~json
curl  -H 'Content-Type: application/json'  -XPOST http://JANSON01:9200/bigdata/product/ -d '{"name":"HADOOP","author" : "Doug Cutting","version":"3.3.3"}'
~~~

##### 局部更新 

使用得最为广泛

要是用_update，同时要更新的是source中的doc内容					
例子：使用post,将索引标识为OSUhuGkBJFmjDtb2b5pO索引信息更新为："author":"小鱼儿"
~~~json
curl -H 'Content-Type:application/json' -XPOST 'http://JANSON01:9200/bigdata/product/OSUhuGkBJFmjDtb2b5pO/_update?pretty'  -d '{ "doc":{"author":"小鱼儿"} }'
~~~
注意：a) _update: 更新的动作（action），在url中，以下划线开头的是动作，es内部赋予了特殊的含义。b) 局部更新时，使用 _update内置的动作，需要带参数：-XPOST

#### CURL使用之查询所有
查询所有 -GET
根据产品ID查询 curl -XGET http://localhost:9200/bigdata/product/1?pretty
在任意的查询url中添加pretty参数，es可以获取更易识别的json结果。
检索文档中的一部分，显示特定的字段内容
curl -XGET http://localhost:9200/bigdata/product/1?source=name,author&pretty'
获取source的数据
curl -XGET 'http://localhost:9200/bigdata/product/1/source?pretty'
查询所有
curl -XGET 'http://localhost:9200/bigdata/product/_search?pretty'
根据条件进行查询
curl -XGET 'http://localhost:9200/bigdata/product/_search?q=name:hbase&pretty'


#### CURL使用之ES更新&删除

ES更新
ES可以使用PUT或者POST对文档进行更新，如果指定ID的文档已经存在，则执行更新操作
注意：执行更新操作的时候，ES首先将旧的文档标记为删除状态，然后添加新的文档，旧的文
档不会立即消失，但是你也无法访问，ES会继续添加更多数据的时候在后台清理已经标记为删
除状态的文档。
局部更新
可以添加新字段或者更新已经存在字段(必须使用POST)
curl -XPOST http://localhost:9200/bigdata/product/1/_update -d 
'{"doc":{"name" : "apache-hadoop"}}'
普通删除，根据主键删除
curl -XDELETE http://localhost:9200/bigdata/product/3/
说明：如果文档存在，es属性found：true，successful:1，_version属性的值+1。如果文档不存在，es属性found为false，但是版本值version依然会+1，这个就是内部,管理的一部分，有点像svn版本号，它保证了我们在多个节点间的不同操作的顺序被正确标记了。注意：一个文档被删除之后，不会立即生效，他只是被标记为已删除。ES将会在你之后添加
更多索引的时候才会在后台进行删除。


#### 批量操作
步骤：
①新建名为bank的索引库
②将待处理的数据上传到linux指定目录下
③进行批量导入操作
curl  -H "Content-Type: application/json" -XPOST 'http://JANSON01:9200/bank/account/_bulk' --data-binary @/home/mike/data/accounts.json

批处理注意点说明：
a) Bulk请求可以在URL中声明/_index或者/_index/_type
b) Bulk一次最大处理多少数据量
Bulk会把将要处理的数据载入内存中，所以数据量是有限制的
最佳的数据量不是一个确定的数值，它取决于你的硬件，你的文档大小以及复杂性，你的索引以及搜索的负载
一般建议是1000~5000个文档，如果你的文档很大，可以适当减少队列，大小建议是5~15MB，默认不能超过100M，可以在es的配置文件中修改这个值
http.max_content_length: 100mb
c) 灵活使用批处理操作，会大幅度提高程序执行的效率，但是，批处理操作的数据量是有一个临界值的，不是没有极限的！

补充说明：
可以查看一下各个索引库信息
curl  'http://janson01:9200/_cat/indices?v'		
[root@JANSON01 ~]# curl 'http://janson01:9200/_cat/indices?v'
health status index        uuid                                             pri     rep        docs.count  docs.deleted  store.size   pri.store.size
yellow open   bigdata   Np2VvkyQQeqtNyzOug8cOA    5       1            5                    0                      19.2kb          19.2kb
yellow open   bank        yrks_XJdSJWJZg0Ibt0GkQ         5       1            1000              0                      482.7kb        482.7kb

ElasticSearch的集群状态:
Green：	所有的主分片和副分片都可用 （主分片： es集群中主节点上的分片；副分片：es集群中从节点上的分片 ）
Yellow：所有的主分片都可用，不是所有的副分片都可用
Red： 不是所有的主分片和副分片都可用		

####  ES版本控制

ES如何实现版本控制(使用es内部版本号)
1：首先得到需要修改的文档，获取版本(version)号
curl -XGET http://localhost:9200/bigdata/product/1
2：再执行更新操作的时候把版本号传过去
curl -XPUT http://localhost:9200/bigdata/product/1?version=1 -d     '{"name":"hadoop","version":3}'(覆盖)
curl -XPOST http://localhost:9200/bigdata/product/1/update?version=3 -d '{"doc":{"name":"apache hadoop","latest_version": 2.6}}'(部分更新)
如： 
~~~json
[jerry@JANSON01 data]$ curl -H 'Content-Type:application/json' -XPOST 'Http://JANSON01:9200/bigdata/product/HM9rL2oB300nRXZV6BRB/_update?version=2&pretty' -d '{"doc":{"author":"楚留香"}}'
{
  "_index" : "bigdata",
  "_type" : "product",
  "_id" : "HM9rL2oB300nRXZV6BRB",
  "_version" : 3,
  "result" : "updated",
  "_shards" : {
    "total" : 2,
    "successful" : 1,
    "failed" : 0
  },
  "_seq_no" : 6,
  "_primary_term" : 2
}
~~~

#### ES插件概述
ES本身服务相对比较少，其功能的强大之处就体现在插件的丰富性上。有非常多的ES插件用于ES的管理，性能的完善，下面就给大家介绍几款常用的插件。
①bigdesk:用于对es集群的健康状况进行实时的监控。
②head：用于使用可视化的方式来操作es集群。
③kibana：用于读取es集群中的索引库中的type信息，并使用可视化的方式予以呈现出来。包含：柱状图，饼状图，折线图，仪表盘等等。



### 第Ⅴ讲

#### BigDesk插件介绍

bigdesk：该工具的Git地址是：https://github.com/lukas-vlcek/bigdesk   ~> 适用对象：es集群的运维人员。
①BigDesk主要提供的是节点的实时状态监控，包括jvm的情况，linux的情况，    elasticsearch的情况，推荐大家使用。
②里面可以看到集群名称，节点列表。内存消耗情况，GC回收情况。可以自由的在各个节点之间进行切换，自动的添加或是移除一些旧的节点。同样可以更改refresh inerval刷新间隔，图标能够显示的数据量。
	
#### BigDesk插件介绍安装：	
①解压下载的bigdesk插件，注意一定不要下载到elasticsearch的plugins目录下 ，可以与elasticsearch的安装目录一致。（旧版本的es插件的安装目录必须是plugins）
②进入到bigdesk的_site目录，在Linux命令行启动：python -m SimpleHTTPServer
或者以后台进程的方式启动：nohup python -m SimpleHTTPServer > /dev/null 2>&1 &   ~>以后台进程的方式启动bigdesk监控服务（监控es集群的）
③启动，访问(web)：http://janson01:8000/#nodes  ~>访问bigdesk插件，bigdesk插件再去访问es服务器（用于监控es服务器的状况）
注意： 
①浏览器的选择，可以使用：火狐，360，chrome,...
②修改一个js脚本：/home/jerry/bigdesk/js/store/BigdeskStore.js
return (major == 1 && minor >= 0 && maintenance >= 0 && (build != 'Beta1' || build != 'Beta2'));
修改为：
return (major >= 1 && minor >= 0 && maintenance >= 0 && (build != 'Beta1' || build != 'Beta2')); 
			 

#### BigDesk插件使用：	
里面可以看到集群名称，节点列表。内存消耗情况，GC回收情况。可以自由的在各个节点之间进行切换，
自动的添加或是移除一些旧的节点。同样可以更改refresh inerval刷新间隔，图标能够显示的数据量。


#### hea插件介绍：	
es head：elasticsearch-head是一个elasticsearch的集群管理工具，它是完全由HTML5编写的独立网页程序，你可以通过插件把它集成到es。 ~> 使用对象：程序员使用
官方的资料： https://github.com/mobz/elasticsearch-head#running-with-built-in-server

安装步骤：(注意：在root用户下安装)
①nodejs npm grunt安装 （在安装html5运行的环境）
yum install nodejs
yum install npm
npm install -g grunt
npm install -g grunt-cli				

下述的命令需要将os的当前目录设定为head插件的根目录（正式开始实施）
cnpm install （或是：npm install -g cnpm --registry=https://registry.npm.taobao.org ； 或者是： npm install  //执行后会生成node_modules文件夹）
注意： ★若是在线安装失败的话，需要手动下载安装包手动安装。

②修改Gruntfile.js
在该文件中添加如下，务必注意不要漏了添加“，”号，这边的hostname:’*’，表示允许所有IP可以访问,此处也可以修改端口号
~~~json
server: {
	options: {
				hostname: '*',
				port: 9100,
				base: '.',
				keepalive: true
			}
	}
~~~

③启动grunt server 
或者是以后台进程的方式启动：
nohup grunt server > /dev/null 2>&1 &

④访问 http://JANSON01:9100   (建议：使用google浏览器访问)



### 第Ⅵ讲

#### ES-Head Plugin使用
详见讲义：5.7、ES-Head Plugin使用

#### ES集群_安装介绍
集群安装非常简单，只要节点同属于一个局域网同一网段，而且集群名称相同，ES就会自动发现其他节点。
主要配置项
~~~yaml
elastaticsearch-6.5.3节点一 -->master
cluster.name: bigdata
http.port: 9200
network.host: 0.0.0.0
elastaticsearch-6.5.3节点二 -->slave01
cluster.name: bigdata
http.port: 19200
network.host: 0.0.0.0
transport.tcp.port: 19300
elastaticsearch-6.5.3节点三 -->slave02
cluster.name: bigdata
http.port: 29200
network.host: 0.0.0.0
transport.tcp.port: 29300
~~~

配置完成之后启动三个ES节点
通过ES插件elasticsearch-head查看集群信息

#### 主题：ES集群_安装（一）
前提：（在另外两台节点上名为jerry的用户）	
①基于单机版，别的节点上安装的前提事先要准备好：
max file descriptors [4096] for elasticsearch process is too low, increase to at least [65536]
②配置jerry用户到另外两台节点的免密码登录
ssh-keygen -t rsa
ssh-copy-id -i jerry@janson02  ~> 将当前节点上的公钥拷贝到别的节点上
③es集群安装注意点：（集群中所有节点都需要配置）
discovery.zen.minimum_master_nodes: 2  <~ 防止“脑裂”（brain split）,集群中至少有两台节点可用，否则，若只有一台，集群就瘫痪，计算公式：数 = 节点数/2 + 1
discovery.zen.ping.unicast.hosts: ["JANSON01", "JANSON02", "JANSON03"]  <~ es集群中有哪些节点， 官方文档上显示：只要集群中的所有节点在同一个网段内，所有
索引服务器彼此感知到，自动组织成一个集群



### 第Ⅶ讲

#### ES集群安装演示（二）
步骤： 
①将JANSON01节点上es目录拷贝到JANSON02,JANSON03上
scp -r ~/es  tom@janson03:~/
mkdir -p /home/tom/data/elastic  <~  新建目录，不能直接从janson01节点拷贝,若是手动拷贝，集群会失效！ (会自动将节点JANSON01上的数据自动同步到别的节点上)
mkdir -p /home/tom/logs/elastic
②修改es核心配置文件elasticsearch.yml
node.name: 集群中当前节点的名字
network.host: ip地址的别名
③验证：(不需要在别的节点上安装插件，因为插件是独立于es服务器单独存在)
通过插件进行验证 
④集群的健康状况：
Green：	所有的主分片和副分片都可用
Yellow：所有的主分片都可以不是所有的副分片都可用
Red：	不是所有的主分片和副分片都可用	


####  ES集群_实操
详见讲义：6.4、ES集群演示

#### ES核心概念_概述
ES核心概念之Cluster
ES核心概念之shards
ES核心概念_replicas
ES核心概念之recovery & gateway
ES核心概念_discovery.zen
ES核心概念_Transport

#### ES核心概念_Cluster
Cluster代表一个集群，集群中有多个节点，其中有一个为主节点，这个主节点是可以通过选举产生的，主从节点是对于集群内部来说的。
ES的一个概念就是去中心化，字面上理解就是无中心节点，这是对于集群外部来说的，因为从外部来看ES集群，在逻辑上是个整体，你与任何一个节点的通信和与整个ES集群通信是等价的。
主节点的职责是负责管理集群状态，包括管理分片的状态和副本的状态，以及节点的发现和删除。
只需要在同一个网段之内启动多个ES节点，就可以自动组成一个集群。
默认情况下ES会自动发现同一网段内的节点，自动组成集群。



### 第Ⅷ讲

#### ES核心概念之shards
代表索引分片，ES可以把一个完整的索引分成多个分片，这样的好处是可以把一个大的索引拆分成多个，分布到不同的节点上，构成分布式搜索。
分片的数量只能在索引创建前指定，并且索引创建后不能更改。
可以在创建索引库的时候指定
curl -XPUT 'localhost:9200/test1/' -d '{"settings":{"number_of_shards":3}}'
默认是一个索引库有5个分片 index.number_of_shards:5

#### ES核心概念之replicas
代表索引副本，ES可以给索引设置副本，副本的作用一是提高系统的容错性，当某个节点某个分片损坏或丢失时可以从副本中恢复。二是提高ES的查询效率，
ES会自动对搜索请求进行负载均衡。
可以在创建索引库的时候指定
curl -XPUT 'localhost:9200/test2/' -d'{"settings":{"number_of_replicas":2}}'
默认是一个分片有1个副本 index.number_of_replicas:1

####  ES核心概念之recovery & gateway

代表数据恢复或者叫数据重新分布，ES在有节点加入或退出时会根据机器的负载对索引分片进行重新分配，挂掉的节点重新启动时也会进行数据恢复。
代表ES索引的持久化存储方式，ES默认是先把索引存放到内存中，当内存满了时再持久化到硬盘。当这个ES集群关闭在重新启动是就会从gateway中读取索引数据。
Es支持多种类型的gateway，有本地文件系统(默认)，分布式文件系统，Hadoop的HDFS和amazon的s3云存储服务。

#### ES核心概念之discovery.zen

代表ES的自动发现节点机制，ES是一个基于p2p的系统，它先通过广播寻找存在的节点，再通过多播协议来进行节点之间的通信，同时也支持点对点的交互。
**如果是不同网段的节点如果组成ES集群
 禁用自动发现机制   discovery.zen.ping.multicast.enabled: false
设置新节点被启动时能够发现的注解列表
discovery.zen.ping.unicast.hosts: ["master:9200", "slave01:9200"]**

