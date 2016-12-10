package com.sree.kafka.utils.connect;

import java.util.Map;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.source.SourceRecord;

/**
 * This class is to represent a Message with its kafka topic offset key , offset
 * value and value schema
 *
 * @author sree
 *
 */
public class ConnectMessageTemplate {
	private String message;
	private String kafkaTopic;
	private Schema valueSchema;
	private Map<String, Object> offsetKey;
	private Map<String, Object> offsetValue;

	/**
	 * Set Message
	 * 
	 * @param message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Set Kafka Topic
	 * 
	 * @param topic
	 */
	public void setKafkaTopic(String topic) {
		this.kafkaTopic = topic;
	}

	/**
	 * Get Kafka Topic
	 * 
	 * @return
	 */
	public String getKafkaTopic() {
		return kafkaTopic;
	}

	/**
	 * Convert Static source message to kafka source record
	 * 
	 * @return
	 */
	public SourceRecord staticMessageToSourceRecord() {
		return new SourceRecord(offsetKey, offsetValue, kafkaTopic, Schema.STRING_SCHEMA, message);
	}

	/**
	 * Convert Real time source message to kafka source record
	 * 
	 * @return
	 */
	public SourceRecord realTimeMesssageToSourceRecord() {
		return new SourceRecord(null, null, kafkaTopic, null, Schema.STRING_SCHEMA, kafkaTopic, Schema.STRING_SCHEMA,
				message);
	}

	/**
	 * Set value schema
	 * 
	 * @param valueSchema
	 */
	public void setValueSchema(Schema valueSchema) {
		this.valueSchema = valueSchema;
	}

	/**
	 * Set offset key
	 * 
	 * @param offsetKey
	 */
	public void setOffsetKey(Map<String, Object> offsetKey) {
		this.offsetKey = offsetKey;
	}

	/**
	 * Set offset value
	 * 
	 * @param offsetValue
	 */
	public void setOffsetValue(Map<String, Object> offsetValue) {
		this.offsetValue = offsetValue;
	}
}
