package com.sree.kafka.connectors.jmx.config;

import java.util.Map;
import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create Kafka JMX default configurations
 * 
 * @author sree
 * 
 */

public class JmxConfigs extends AbstractConfig {
	private static Logger logger = LoggerFactory.getLogger(JmxConfigs.class);

	/**
	 * Create default connector configurations. In case if we dont provide some
	 * property in property file- it will take from this default confs
	 * 
	 * @return
	 */
	public static ConfigDef baseConfigDef() {
		return new ConfigDef()
				.define(JmxConstants.SERVICE, Type.STRING, "kafka", Importance.HIGH,
						"JMX Service name- Kafka,Flink,Cassandra etc..")
				.define(JmxConstants.ZOOKEEPER_HOST, Type.STRING, "localhost:2181", Importance.HIGH,
						"Zookeeper Host to connect , if service name is kafka")
				.define(JmxConstants.JMX_URL, Type.STRING, "localhost:9999", Importance.HIGH, "JMX URL to connect")
				.define(JmxConstants.JMX_USERNAME, Type.STRING, "", Importance.MEDIUM, "Username to connect to JMX")
				.define(JmxConstants.JMX_PASSWORD, Type.STRING, "", Importance.MEDIUM, "Password to connect to JMX")
				.define(JmxConstants.KAFKA_TOPIC, Type.STRING, "jmx", Importance.HIGH, "Kafka topic to store metrics")
				.define(JmxConstants.JMX_WAIT_TIMEOUT, Type.LONG, 3000, Importance.LOW, "JMX wait timeout")
				.define(JmxConstants.JMX_FETCH_TIMEOUT, Type.LONG, 3000, Importance.LOW, "JMX fetch timeout")
				.define(JmxConstants.RMI_CONNECT_TIMEOUT, Type.LONG, 3000, Importance.LOW, "RMI connect timeout")
				.define(JmxConstants.RMI_HANDSHAKE_TIMEOUT, Type.LONG, 3000, Importance.LOW, "RMI handshake timeout")
				.define(JmxConstants.RMI_RESPONSE_TIMEOUT, Type.LONG, 3000, Importance.LOW, "RMI response timeout");
	}

	public static ConfigDef config = baseConfigDef();

	public JmxConfigs(Map<String, String> properties) {
		super(config, properties);
		logger.info("Kafka JMX Connect default properties initialized.");
	}
}
