* docker-compose up
* install canal.kafka-1.1.0
* configure canal
> canal.properties配置：
 ```
 canal.id= 1
 canal.ip=
 canal.port=11111
 canal.metrics.pull.port=11112
 canal.zkServers=127.0.0.1:2181
 canal.serverMode=kafka
 ```
> instance.properties配置
```
canal.instance.master.address=127.0.0.1:3320
# username/password
canal.instance.dbUsername=root
canal.instance.dbPassword=1q2w3e4r5t
canal.instance.connectionCharset=UTF-8
```
> kafka.yaml配置
```
servers: 127.0.0.1:9092
retries: 0
batchSize: 16384
lingerMs: 1
bufferMemory: 33554432
# canal的批次大小，单位 k，量大建议改为1M
canalBatchSize: 50
filterTransactionEntry: true

canalDestinations:
  - canalDestination: example
    topic: example
#    partition: 1
    # 一个destination可以对应多个topic
#    topics:
#      - topic: example
#        partition:
```

> Mysql配置(Docker已经配置好)
```
[mysqld]
log-bin=mysql-bin #添加这一行就ok  
binlog-format=ROW #选择row模式  
server_id=1 #配置mysql replaction需要定义，不能和canal的slaveId重复 
```

* Use mysql client to connect mysql in docker
* Create database and tables in the mysql
* Start canal: bin/startup.sh
* Start com.github.canalclientdemo.BinlogConsumer
* Insert/Update records in the database and then watch the output of BinlogConsumer

