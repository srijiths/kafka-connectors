package com.sree.kafka.connectors.jmx;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * JMX Client class which can initialize connection to remote JMX Server
 * 
 * @author sree
 * 
 * 
 */

public class JmxClient {
	private static Logger logger = LoggerFactory.getLogger(JmxClient.class);
	JMXConnector jmxConnector;
	public MBeanServerConnection mBeanServer;

	/**
	 * Instantiate JMX connection
	 * 
	 * @param jmxUrl
	 * @param environment
	 */
	public MBeanServerConnection initializeJmxClient(String jmxUrl, Map environment) {
		try {
			System.setProperty("java.rmi.server.hostname", jmxUrl.split(":")[0]);
			jmxConnector = JMXConnectorFactory.connect(new JMXServiceURL(jmxUrl), environment);
			mBeanServer = jmxConnector.getMBeanServerConnection();
			logger.info("JMX Connection and MBean Server successfully initialized to " + jmxUrl);
		} catch (MalformedURLException e) {
			logger.error("Malformed JMX URL Exception : ", e);
		} catch (IOException e) {
			logger.error("IO Exception : ", e);
		}
		return mBeanServer;
	}

	/**
	 * Close JMX connection
	 */
	public void close() {
		try {
			jmxConnector.close();
		} catch (IOException e) {
			logger.error("JMX Connector closing exception  : ", e);
		}
	}

	/**
	 * Get the complete list of beans registered in JMX
	 * 
	 * @param mbeanConnection
	 * @return
	 */
	public Set getCompleteMbeans(MBeanServerConnection mbeanConnection) {
		Set beans = null;
		try {
			beans = mbeanConnection.queryNames(null, null);
		} catch (IOException e) {
			logger.error("Not able to get complete beans {} ", e);
		}
		return beans;
	}

	/**
	 * Get each bean and create a JSON output which contains all the bean
	 * parameters
	 * 
	 * @param beans
	 * @param mBeanServer
	 * @return
	 */
	public List<String> getMetricsFromMbean(Set beans, MBeanServerConnection mBeanServer, String serviceName) {
		List<String> beanList = new ArrayList<String>();
		for (Object obj : beans) {
			try {
				JSONObject bean = new JSONObject();
				ObjectName beanName = null;
				if (obj instanceof ObjectName)
					beanName = (ObjectName) obj;
				else if (obj instanceof ObjectInstance)
					beanName = ((ObjectInstance) obj).getObjectName();

				MBeanInfo mBeanInfo = mBeanServer.getMBeanInfo(beanName);
				if (!beanName.getDomain().contains(serviceName))
					continue;

				bean.put("metric_group", beanName.getDomain());
				bean.put("metric_servicename", serviceName);
				String metricNamespace = beanName.getDomain().replaceAll("\\.", "_");
				Hashtable<String, String> properties = beanName.getKeyPropertyList();
				for (Map.Entry<String, String> prop : properties.entrySet()) {
					if (prop.getKey().toLowerCase().equalsIgnoreCase("name")) {
						metricNamespace = metricNamespace + "_" + prop.getValue();
					} else
						bean.put(String.format("metric_%s", prop.getKey().toLowerCase()), prop.getValue());
				}

				// Get all attributes and its values
				try {
					MBeanInfo beanInfo = mBeanServer.getMBeanInfo(beanName);
					MBeanAttributeInfo[] attrInfo = beanInfo.getAttributes();
					for (MBeanAttributeInfo attr : attrInfo) {
						try {
							bean.put(metricNamespace + "_" + attr.getName(),
									mBeanServer.getAttribute(beanName, attr.getName()).toString());
						} catch (Exception e) {
							logger.error("Attr parsing exception {} ", e);
						}
					}
				} catch (Exception e) {
					logger.error("Get Bean attribute exception {} ", e);
				}

				beanList.add(bean.toString());
			} catch (Exception e) {
				logger.error("Get Metrics From bean exception {} ", e);
			}
		}

		return beanList;
	}

}
