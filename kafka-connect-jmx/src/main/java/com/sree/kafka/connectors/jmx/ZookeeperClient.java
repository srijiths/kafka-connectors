package com.sree.kafka.connectors.jmx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Class to initialte zookeeper connection and zookeepr activities
 * 
 * @author sree
 * 
 *
 */
public class ZookeeperClient {
	private static Logger logger = LoggerFactory.getLogger(ZookeeperClient.class);
	ZooKeeper zooClient;
	final CountDownLatch connSignal = new CountDownLatch(1);

	/**
	 * Initiate the connection
	 * 
	 * @param zooHost
	 *            , contains both host and port in host:port format
	 * @param zooHost
	 * @param timeout
	 * @param watcher
	 * @return
	 * @throws IOException
	 */
	public ZooKeeper initializeClient(String zooHost, int timeout) throws Exception {
		zooClient = new ZooKeeper(zooHost, timeout, new Watcher() {
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected) {
					connSignal.countDown();
				}
			}
		});
		connSignal.await();
		return zooClient;
	}

	/**
	 * Initiate the connection
	 * 
	 * @param zooHost
	 * @return
	 * @throws IOException
	 */
	public ZooKeeper initializeClient(String zooHost) throws Exception {
		zooClient = new ZooKeeper(zooHost, 10000, new Watcher() {
			public void process(WatchedEvent event) {
				if (event.getState() == KeeperState.SyncConnected) {
					connSignal.countDown();
				}
			}
		});
		connSignal.await();
		return zooClient;
	}

	/**
	 * Get the Kafka brokers using zookeeper automatic discovery Returns a list
	 * of IP's which contains Kafka Broekrs:JMX_PORT
	 * 
	 * @param zooHost
	 * @param jmxPortRequired
	 * @return
	 * @throws Exception
	 */
	public List<String> getKafkaHostsFromZookeeper(String zooHost) throws Exception {
		List<String> kafkaHosts = new ArrayList<String>();
		ZooKeeper zk = initializeClient(zooHost);
		List<String> ids = zk.getChildren("/brokers/ids", false);
		for (String id : ids) {
			try {
				String brokerInfo = new String(zk.getData("/brokers/ids/" + id, false, null));
				JSONObject jsonObj = new JSONObject(brokerInfo);
				if (jsonObj.has("host")) {
					if (jsonObj.has("jmx_port")) {
						kafkaHosts.add(jsonObj.get("host").toString() + ":" + jsonObj.get("jmx_port").toString());
					}
				}
			} catch (Exception e) {
				logger.error("Zookeeper borker getting exception {}", e);
			}
		}
		return kafkaHosts;
	}

	public void close(ZooKeeper zoo) throws InterruptedException {
		zoo.close();
	}
}
