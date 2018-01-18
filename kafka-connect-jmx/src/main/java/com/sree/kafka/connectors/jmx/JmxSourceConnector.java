package com.sree.kafka.connectors.jmx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigException;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sree.kafka.connectors.jmx.config.JmxConfigs;
import com.sree.kafka.utils.Version;

/**
 * Jmx Connector class
 * 
 * @author sree
 *
 */

public class JmxSourceConnector extends SourceConnector {
	private static final Logger logger = LoggerFactory.getLogger(JmxSourceConnector.class);
	private Map<String, String> configProperties;
	JmxConfigs configs;

	/**
	 * Set the configs as {@link ConfigDef}}
	 */
	@Override
	public ConfigDef config() {
		return JmxConfigs.config;
	}

	/**
	 * Initialize the property file and create a Map<String,String>
	 */
	@Override
	public void start(Map<String, String> props) {
		this.configProperties = props;
		try {
			configs = new JmxConfigs(props);
		} catch (ConfigException e) {
			logger.error("Configuration Exception ", e);
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	/**
	 * Actual call to the JMX Task
	 */
	@Override
	public Class<? extends Task> taskClass() {
		return JmxTask.class;
	}

	/**
	 * Task Configs :- creating task config for each JMX Task
	 */
	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		List<Map<String, String>> taskConfigs = new ArrayList<Map<String, String>>();
		Map<String, String> taskProps = new HashMap<String, String>(configProperties);
		for (int i = 0; i < maxTasks; i++) {
			taskConfigs.add(taskProps);
		}
		return taskConfigs;
	}

	@Override
	public String version() {
		return Version.getVersion();
	}
}
