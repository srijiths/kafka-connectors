package com.sree.kafka.utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common String utility methods.
 * 
 * @author sree
 * 
 */

public class TextUtils {
	private static final Logger logger = LoggerFactory.getLogger(TextUtils.class);

	/**
	 * Validate whether a string is null or empty
	 * 
	 * @param input
	 * @return
	 */
	public static boolean isNullOrEmpty(String input) {
		if (input == null)
			return true;
		if (input.length() == 0)
			return true;
		return false;
	}

	/**
	 * Convert the input bytes to String
	 * 
	 * @param inputBytes
	 * @return
	 */
	public String getFileContent(byte[] inputBytes) {
		StringBuilder stringBuilder = new StringBuilder();
		try {
			InputStream inputStream = new ByteArrayInputStream(inputBytes);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
			String line = bufferedReader.readLine();
			while (line != null) {
				stringBuilder.append(line);
				stringBuilder.append('\n');
				line = bufferedReader.readLine();
			}
		} catch (Exception e) {
			logger.error("Get file content exception ", e);
		}
		return stringBuilder.toString();
	}
}
