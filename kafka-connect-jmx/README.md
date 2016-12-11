kafka-connect-jmx
----------------
kafka-connect-jmx collects JMX metrics , convert it to a readable JSON and push to kafka. This connector can connect any JMX enabled services. 

* To collect JMX metrics for kafka , then just specify ` zookeeper.host ` in the configuration. The connector will automatically collect all kafka brokers and JMX port registered in zookeeper and use it for metrics collection.

* If you are collecting metrics other than kafka, then you have to specify all JMX urls in ` jmx.url ` separated by comma.

Configuration
--------------------
* Name of the connector
	` name=jmx-source `
* Connector class to invoke the connector
	` connector.class=com.sree.kafka.connectors.jmx.JmxConnector `
* Maximum number of tasks
	` tasks.max=1 `
* Kafka topic to push the messages
	` kafka.topic=jmx-test `
* JMX is running for which application ?  If you want JMX metrics for Kafka , then ` jmx.servicename=kafka `. If you want JMX metrics for Flink , then ` jmx.servicename=flink ` etc.. This configuration helps you group the metrics coming from different JMX services.
* If ` jmx.servicename ` is kafka , then you have to provide zookeeper.host, else zookeeper.host parameter is not required.
	` zookeeper.host=localhost:2181 `
* If ` jmx.servicename ` is not kafka , then below property is mandatory. Provide the full JMX URL separated by comma
	` jmx.url=54.238.221.37:8080,54.238.237.66:8080 `
	
* Advanced Configurations
	* JMX username `jmx.username=test`
	* JMX password `jmx.password=test`
	* Wait time out `jmx.wait_timeout=3000`
	* Fetch time out `jmx.fetch_timeout=3000`
	* RMI connect time out `rmi.connect_timeout=3000`
	* RMI handshake time out `rmi.handshake_timeout=3000`
	* RMI response time out `rmi.response_timeout=3000` 
	
	
Sample Output
--------------
JMX connector outputs a flattend JSON like below.

* ` metric_servicename ` to identify the jmx configured service.
* ` metric_group  ` is the JMX metrics group.
* ` metric_type  ` , is the JMX metrics type under the group.
* All other JMX attributes are prefixed with value of ` metric_servicename_metric_group  `

Note : If you need a JSON format other than listed below, you can modify ` JmxClient ` class's ` getMetricsFromMbean ` method.

```javascript

{
  "kafka_network_ResponseQueueTimeMs_99thPercentile": "0.0",
  "kafka_network_ResponseQueueTimeMs_98thPercentile": "0.0",
  "kafka_network_ResponseQueueTimeMs_Max": "0.0",
  "metric_servicename": "kafka",
  "metric_request": "SaslHandshake",
  "kafka_network_ResponseQueueTimeMs_Count": "0",
  "kafka_network_ResponseQueueTimeMs_StdDev": "0.0",
  "metric_group": "kafka.network",
  "kafka_network_ResponseQueueTimeMs_999thPercentile": "0.0",
  "kafka_network_ResponseQueueTimeMs_50thPercentile": "0.0",
  "kafka_network_ResponseQueueTimeMs_Min": "0.0",
  "metric_type": "RequestMetrics",
  "kafka_network_ResponseQueueTimeMs_95thPercentile": "0.0",
  "kafka_network_ResponseQueueTimeMs_75thPercentile": "0.0",
  "kafka_network_ResponseQueueTimeMs_Mean": "0.0"
}

```

Build
----------------

Using Maven , mvn clean install

License
----------------

Apache License 2 - http://www.apache.org/licenses/LICENSE-2.0.html