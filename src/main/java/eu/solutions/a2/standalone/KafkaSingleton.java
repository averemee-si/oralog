/**
 * Copyright (c) 2018-present, http://a2-solutions.eu
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package eu.solutions.a2.standalone;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.logs.oracle.audit.OraAuditPojo;
import eu.solutions.a2.logs.oracle.audit.OraAuditRecordPojo;
import eu.solutions.a2.logs.oracle.audit.OraAuditTraceNameHolder;
import eu.solutions.a2.utils.ExceptionUtils;


public class KafkaSingleton extends BaseSingleton implements SendMethodIntf {

	private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSingleton.class);
	private static final String SECURITY_SSL = "SSL";
	private static final String SECURITY_SASL_SSL = "SASL_SSL";

	private static KafkaSingleton instance;

	/** Kafka topic */
	private String kafkaTopic = null;
	/**  Kafka producer */
	private Producer<String, byte[]> kafkaProducer;

	private KafkaSingleton() {}

	public static KafkaSingleton getInstance() {
		if (instance == null) {
			instance = new KafkaSingleton();
		}
		return instance;
	}

	@Override
	public Runnable oraAdrJob(final String key, final String envelope) {
		Runnable task = () -> {
			final long startTime = System.currentTimeMillis();
			try {
				ProducerRecord<String, byte[]> producerRecord = null;
				if (dataFormat == Constants.DATA_FORMAT_JSON) {
					producerRecord = 
							new ProducerRecord<>(kafkaTopic, key,
									writer.writeValueAsBytes(readerAdr.readValue(envelope)));
				} else {
					producerRecord = 
							new ProducerRecord<>(kafkaTopic, key,
									envelope.getBytes());
				}
				final int dataLength = producerRecord.value().length;
				kafkaProducer.send(
						producerRecord,
						(metadata, exception) -> {
							if (metadata == null) {
								// Error occured
								LOGGER.error("Exception while sending {} to Kafka.",  key);
								LOGGER.error("Message data are:\n\t{}", envelope);
								LOGGER.error(ExceptionUtils.getExceptionStackTrace(exception));
							} else {
								CommonJobSingleton.getInstance().addRecordData(
										dataLength,
										System.currentTimeMillis() - startTime);
							}
						});
			} catch (IOException jpe) {
				LOGGER.error(ExceptionUtils.getExceptionStackTrace(jpe));
			}
		};
		return task;
	}

	@Override
	public Runnable oraAudJob(
			final OraAuditTraceNameHolder trcFile,
			final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles) {
		Runnable task = () -> {
			final long startTime = System.currentTimeMillis();
			try {
				final File auditFile = new File(trcFile.getFileName());
				final BufferedReader br = new BufferedReader(new FileReader(auditFile));

				String line = null;
				final StringBuilder sb = new StringBuilder();
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}
				br.close();
				final String value = sb.toString();
				if (value.trim().endsWith("</Audit>")) {
					if (dataFormat == Constants.DATA_FORMAT_JSON) {
						OraAuditPojo oap = readerAud.readValue(value);
						for (OraAuditRecordPojo auditRecord : oap.getAuditRecord()) {
							final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(
									kafkaTopic,
									trcFile.getFileName() + ":" + auditRecord.getEntryId(),
									writer.writeValueAsBytes(auditRecord));
							final int dataLength = producerRecord.value().length;
							kafkaProducer.send(
									producerRecord,
									(metadata, exception) -> {
										if (metadata == null) {
											// Error occured
											LOGGER.error("Exception while sending {} to Kafka.", trcFile.getFileName());
											try {
												LOGGER.error("Message data are:\n\t{}", writer.writeValueAsString(auditRecord));
											} catch (JsonProcessingException e) {}
											LOGGER.error(ExceptionUtils.getExceptionStackTrace(exception));
										} else {
											CommonJobSingleton.getInstance().addRecordData(
													dataLength,
													System.currentTimeMillis() - startTime);
										}
									});
						}
						//TODO - need better handling here...
						try {
							Files.delete(auditFile.toPath());
						} catch (IOException ioe) {
							LOGGER.error("Exception while deleting {}!!!", trcFile.getFileName());
							LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
						}
					} else {
						// Send RAW data
						final ProducerRecord<String, byte[]> producerRecord = new ProducerRecord<>(kafkaTopic, trcFile.getFileName(),
								value.getBytes());
						final int dataLength = producerRecord.value().length;
						kafkaProducer.send(
								producerRecord,
								(metadata, exception) -> {
									if (metadata == null) {
										// Error occured
										LOGGER.error("Exception while sending {} to Kafka.", trcFile.getFileName());
										LOGGER.error("Message data are:\n\t{}", value);
										LOGGER.error(ExceptionUtils.getExceptionStackTrace(exception));
									} else {
										try {
											Files.delete(auditFile.toPath());
										} catch (IOException ioe) {
											LOGGER.error("Exception while deleting {}!!!", trcFile.getFileName());
											LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
										}
										CommonJobSingleton.getInstance().addRecordData(
												dataLength,
												System.currentTimeMillis() - startTime);
									}
								});
					}
				} else {
					LOGGER.error("Mailformed or incomplete xml file: {}", trcFile.getFileName());
					if (lockedFiles.contains(trcFile)) {
						LOGGER.error("Unhanled concurrency issue with xml file: {}", trcFile.getFileName());
					} else {
//						LOGGER.error("Adding {} to queue again", trcFile.getFileName());
						//TODO do we need to add this file again???
					}
				}
			} catch (IOException jpe) {
				LOGGER.error(ExceptionUtils.getExceptionStackTrace(jpe));
			}
		};
		return task;
	}

	public void shutdown() {
		if (kafkaProducer != null) {
			kafkaProducer.flush();
			kafkaProducer.close();
		} else {
			LOGGER.error("Attempt to close non-initialized Kafka producer!");
			System.exit(1);
		}
	}

	public void parseSettings(final Properties props, final String configPath, final int exitCode) {
		kafkaTopic = props.getProperty("a2.kafka.topic");
		if (kafkaTopic == null || "".equals(kafkaTopic)) {
			LOGGER.error("a2.kafka.topic parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		String kafkaServers = props.getProperty("a2.kafka.servers");
		if (kafkaServers == null || "".equals(kafkaServers)) {
			LOGGER.error("a2.kafka.servers parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		String kafkaClientId = props.getProperty("a2.kafka.client.id");
		if (kafkaClientId == null || "".equals(kafkaClientId)) {
			LOGGER.error("a2.kafka.client.id parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		/** Proprties for building Kafka producer */
		Properties kafkaProps = new Properties();
		kafkaProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
		kafkaProps.put(ProducerConfig.CLIENT_ID_CONFIG, kafkaClientId);
		kafkaProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		kafkaProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());

		final String useSSL = props.getProperty("a2.kafka.security.protocol", "").trim();
		if (SECURITY_SSL.equalsIgnoreCase(useSSL)) {
			kafkaProps.put("security.protocol", SECURITY_SSL);
			kafkaProps.put("ssl.truststore.location", props.getProperty("a2.kafka.security.truststore.location"));
			kafkaProps.put("ssl.truststore.password", props.getProperty("a2.kafka.security.truststore.password"));
		} else if (SECURITY_SASL_SSL.equalsIgnoreCase(useSSL)) {
			kafkaProps.put("security.protocol", SECURITY_SASL_SSL);
			kafkaProps.put("ssl.truststore.location", props.getProperty("a2.kafka.security.truststore.location"));
			kafkaProps.put("ssl.truststore.password", props.getProperty("a2.kafka.security.truststore.password"));
			kafkaProps.put("sasl.mechanism", "PLAIN");
			kafkaProps.put("sasl.jaas.config", props.getProperty("a2.security.jaas.config"));
		}

		String optParam = null;
		optParam = props.getProperty("a2.kafka.compression.type", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, optParam);
		} else {
			/** Set to gzip by default */
			kafkaProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "gzip");
		}

		optParam = props.getProperty("a2.kafka.batch.size", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.BATCH_SIZE_CONFIG, optParam);
		}
		optParam = props.getProperty("a2.kafka.linger.ms", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.LINGER_MS_CONFIG, optParam);
		}
		optParam = props.getProperty("a2.kafka.acks", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.ACKS_CONFIG, optParam);
		}
		optParam = props.getProperty("a2.kafka.max.request.size", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, optParam);
		}
		optParam = props.getProperty("a2.kafka.buffer.memory", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, optParam);
		}
		optParam = props.getProperty("a2.kafka.retries", "").trim();
		if (!"".equals(optParam)) {
			kafkaProps.put(ProducerConfig.RETRIES_CONFIG, optParam);
		}

		// Initialize connection to Kafka
		kafkaProducer = new KafkaProducer<>(kafkaProps);
	}

}
