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

* Use mysql client to connect mysql in docker
* Create database and tables in the mysql
* Start canal: bin/startup.sh
* Start com.github.canalclientdemo.BinlogConsumer
* Insert/Update records in the database and then watch the output of BinlogConsumer