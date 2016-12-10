package com.sree.kafka.connectors.jmx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sree.kafka.connectors.jmx.config.JmxConfigs;
import com.sree.kafka.connectors.jmx.config.JmxConstants;
import com.sree.kafka.utils.TextUtils;
import com.sree.kafka.utils.Version;
import com.sree.kafka.utils.connect.ConnectMessageTemplate;

/**
 * 
 * Kafka JMX Task which will - Establish JMX connection - Get all available
 * MBeans - Filter MBeans based on jmx.servicename - Create corresponding JSON -
 * Push the output JSON to kafka.topic
 * 
 * @author sree
 * 
 * 
 */

public class JmxTask extends SourceTask {
	private static final Logger logger = LoggerFactory.getLogger(JmxTask.class);
	List<MBeanServerConnection> jmxServers = new ArrayList<MBeanServerConnection>();
	BlockingQueue<ConnectMessageTemplate> jmxQueue = new LinkedBlockingQueue<ConnectMessageTemplate>();
	JmxConfigs jmxConfig;
	String kafkaTopic;
	String userName;
	String password;
	long jmxWaitTimeout;
	long jmxFetchTimeout;
	long rmiConnectTimeout;
	long rmiHandshakeTimeout;
	long rmiResponseTimeout;
	String jmxService;
	JmxClient jmxClient;

	/**
	 * Get Version of the connector
	 */
	public String version() {
		return Version.getVersion();
	}

	/**
	 * This method repeatedly call at a certain interval Get all the JSON JMX
	 * metrics Insert it to Queue Poll from Queue and push it to kafka.topic
	 */
	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		List<SourceRecord> records = new ArrayList<SourceRecord>();
		for (MBeanServerConnection jmxConn : jmxServers) {
			try {
				Set beans = jmxClient.getCompleteMbeans(jmxConn);
				List<String> metricsOutput = new ArrayList<String>();
				try {
					metricsOutput = jmxClient.getMetricsFromMbean(beans, jmxConn, jmxService);
					for (String bean : metricsOutput) {
						try {
							ConnectMessageTemplate jmxMessage = new ConnectMessageTemplate();
							jmxMessage.setMessage(bean);
							jmxMessage.setKafkaTopic(kafkaTopic);
							jmxQueue.add(jmxMessage);
						} catch (Exception e) {
							logger.error("Adding bean output to JmxMessageProcessor failed {} ", e);
						}
					}
				} catch (Exception e) {
					logger.error("Create Metrics output exception {} ", e);
				}
			} catch (Exception e) {
				logger.error("JMX bean source record creation exception {} ", e);
			}
		}
		while (!jmxQueue.isEmpty()) {
			ConnectMessageTemplate jmxMsg = jmxQueue.poll();
			records.add(jmxMsg.realTimeMesssageToSourceRecord());
		}
		return records;
	}

	/**
	 * Configuration and global variables initialization
	 */
	@Override
	public void start(Map<String, String> props) {
		jmxClient = new JmxClient();
		try {
			jmxConfig = new JmxConfigs(props);
		} catch (ConfigException e) {
			logger.error("Couldn't start " + JmxConnector.class.getName() + " due to configuration error.", e);
		}
		kafkaTopic = jmxConfig.getString(JmxConstants.KAFKA_TOPIC);
		jmxService = jmxConfig.getString(JmxConstants.SERVICE);

		if (jmxService.equalsIgnoreCase("kafka")) {
			initializeKafkaJmxConnector();
		} else {
			initializeJmxConnector();
		}
	}

	/**
	 * Initialize JMX connector for any other services jmx.servicename is
	 * flink/redis/cassandra etc get the connection to each jmx.url
	 */
	private void initializeJmxConnector() {
		String jmxUrl = jmxConfig.getString(JmxConstants.JMX_URL);
		List<String> jmxHosts = new ArrayList<String>();
		jmxHosts = Arrays.asList(jmxUrl.trim().split(","));
		getMBeansFromJmx(jmxHosts);
	}

	/**
	 * jmx.servicename is kafka. Initialize JMX connectivity to Kafka Get the
	 * connection to each kafka broker
	 */
	private void initializeKafkaJmxConnector() {
		String zooHost = jmxConfig.getString(JmxConstants.ZOOKEEPER_HOST);
		List<String> kafkaBrokers = new ArrayList<String>();
		ZookeeperClient zkClient = new ZookeeperClient();
		try {
			kafkaBrokers = zkClient.getKafkaHostsFromZookeeper(zooHost);
			getMBeansFromJmx(kafkaBrokers);
		} catch (Exception e) {
			logger.error("IO Exception in Task Start {} ", e);
		}
	}

	/**
	 * Initialing JMX client for each kafka broker Keep it as a List of
	 * MBeanServerConnection
	 */
	private void getMBeansFromJmx(List<String> hosts) {
		for (String host : hosts) {
			try {
				MBeanServerConnection mBeans = jmxClient.initializeJmxClient(generateJmxURL(host),
						generateJmxEnvironment());
				jmxServers.add(mBeans);
			} catch (Exception e) {
				logger.error("JMX Client connection exception {} ", e);
			}
		}
	}

	/**
	 * Generate JMX URL for each kafka brokers discovered using zookeeper
	 * 
	 * @return
	 */
	private String generateJmxURL(String kafkaBroker) {
		String jmxURL = "";
		try {
			jmxURL = "service:jmx:rmi:///jndi/rmi://" + kafkaBroker + "/jmxrmi";
		} catch (Exception e) {
			logger.error("JMX URL generation failed : ", e);
		}
		return jmxURL;
	}

	/**
	 * Create JMX Environment and Timeout settings
	 * 
	 * @return
	 */
	private Map generateJmxEnvironment() {
		Map jmxEnv = new HashMap();
		try {
			jmxWaitTimeout = jmxConfig.getLong(JmxConstants.JMX_WAIT_TIMEOUT);
			jmxFetchTimeout = jmxConfig.getLong(JmxConstants.JMX_FETCH_TIMEOUT);
			rmiConnectTimeout = jmxConfig.getLong(JmxConstants.RMI_CONNECT_TIMEOUT);
			rmiHandshakeTimeout = jmxConfig.getLong(JmxConstants.RMI_HANDSHAKE_TIMEOUT);
			rmiResponseTimeout = jmxConfig.getLong(JmxConstants.RMI_RESPONSE_TIMEOUT);

			jmxEnv.put("jmx.remote.x.request.waiting.timeout", Long.toString(jmxWaitTimeout));
			jmxEnv.put("jmx.remote.x.notification.fetch.timeout", Long.toString(jmxFetchTimeout));
			jmxEnv.put("sun.rmi.transport.connectionTimeout", Long.toString(rmiConnectTimeout));
			jmxEnv.put("sun.rmi.transport.tcp.handshakeTimeout", Long.toString(rmiHandshakeTimeout));
			jmxEnv.put("sun.rmi.transport.tcp.responseTimeout", Long.toString(rmiResponseTimeout));

			userName = jmxConfig.getString(JmxConstants.JMX_USERNAME);
			password = jmxConfig.getString(JmxConstants.JMX_PASSWORD);

			if (!TextUtils.isNullOrEmpty(userName) && !TextUtils.isNullOrEmpty(password)) {
				String[] creds = { userName, password };
				jmxEnv.put(JMXConnector.CREDENTIALS, creds);
			}
		} catch (Exception e) {
			logger.error("JMX Environment Exception ", e);
		}

		return jmxEnv;
	}

	/**
	 * Stop the JMX Task
	 */
	@Override
	public void stop() {
		logger.info("Stopping JMX Kafka Source..");
		jmxClient.close();
	}
}
