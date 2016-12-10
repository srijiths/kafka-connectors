package com.sree.kafka.utils;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Get the version of the project. version.properties file should be with in the
 * packaged jar under resources folder
 * 
 * @author sree
 * 
 **/
public class Version {
	private static final Logger logger = LoggerFactory.getLogger(Version.class);
	private static String version = "unknown";
	static {
		try {
			Properties props = new Properties();
			props.load(Version.class.getResourceAsStream("/version.properties"));
			version = props.getProperty("version", version).trim();
		} catch (Exception e) {
			logger.warn("Error while loading version:", e);
		}
	}

	/**
	 * Get project version
	 * 
	 * @return
	 */
	public static String getVersion() {
		return version;
	}
}