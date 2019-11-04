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
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.logs.oracle.audit.OraAuditPojo;
import eu.solutions.a2.logs.oracle.audit.OraAuditRecordPojo;
import eu.solutions.a2.logs.oracle.audit.OraAuditTraceNameHolder;
import eu.solutions.a2.utils.ExceptionUtils;
import eu.solutions.a2.utils.GzipUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kinesis.KinesisAsyncClient;
import software.amazon.awssdk.services.kinesis.model.PutRecordRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequest;
import software.amazon.awssdk.services.kinesis.model.PutRecordsRequestEntry;

	
public class KinesisSingleton extends BaseSingleton implements SendMethodIntf {

	private static final Logger LOGGER = LoggerFactory.getLogger(KinesisSingleton.class);

	private static KinesisSingleton instance;

	/** Kinesis stream name */
	private String streamName = null;
	/**  Kinesis Async client */
	private KinesisAsyncClient kinesisClient;
	/** a2.kinesis.file.size.threshold */
	private int fileSizeThreshold = 512;

	private static final int KINESIS_BATCH_SIZE = 500;
	private static final int KINESIS_BATCH_MAX_BYTES = 4_500_000;

	private KinesisSingleton() {}

	public static KinesisSingleton getInstance() {
		if (instance == null) {
			instance = new KinesisSingleton();
		}
		return instance;
	}

	@Override
	public Runnable oraAdrJob(final String key, final String envelope) {
		Runnable task = () -> {
			long startTime = System.currentTimeMillis();
			try {
				byte[] messageData = null;
				if (dataFormat == Constants.DATA_FORMAT_JSON) {
					messageData = writer.writeValueAsBytes(readerAdr.readValue(envelope));
				} else {
					messageData = envelope.getBytes();
				}
				if (messageData.length > fileSizeThreshold) {
					messageData = GzipUtils.compress(writer.writeValueAsString(envelope));
				}
				final int messageLength = messageData.length;
				PutRecordRequest putRecordRequest  = PutRecordRequest.builder()
					.streamName(streamName)
					.partitionKey(key)
					.data(SdkBytes.fromByteArray(messageData))
					.build();

				kinesisClient.putRecord(putRecordRequest)
					.whenComplete((resp, err) -> {
						if (resp != null) {
							CommonJobSingleton.getInstance().addRecordData(
								messageLength,
								System.currentTimeMillis() - startTime);
						} else {
							LOGGER.error("Exception while sending {} to Kinesis.", key);
							LOGGER.error(ExceptionUtils.getExceptionStackTrace(new Exception(err)));
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
						int batchCount = 0;
						int totalCount = 0;
						int totalSize = 0;
						final int recordsTotal = oap.getAuditRecord().size();
						List <PutRecordsRequestEntry> prreList = null;
						for (OraAuditRecordPojo auditRecord : oap.getAuditRecord()) {
							//TODO - compress before send to Kinesis?
							if (batchCount == 0) {
								prreList = new ArrayList<>();
								totalSize = 0;
							}
							final byte[] recordData = writer.writeValueAsBytes(auditRecord);
							totalSize += recordData.length;
							PutRecordsRequestEntry prre = PutRecordsRequestEntry
									.builder()
									.partitionKey(trcFile.getFileName() + ":" + auditRecord.getEntryId())
									.data(SdkBytes.fromByteArray(recordData))
									.build();
							prreList.add(prre);
							batchCount++;
							totalCount++;
							if (batchCount == KINESIS_BATCH_SIZE || 
									totalCount == recordsTotal ||
									totalSize >= KINESIS_BATCH_MAX_BYTES) {
								final int finalBatchCount = batchCount;
								final long finalTotalSize = totalSize;
								PutRecordsRequest putRecordsRequest  = PutRecordsRequest.builder()
										.streamName(streamName)
										.records(prreList)
										.build();
								kinesisClient.putRecords(putRecordsRequest)
									.whenComplete((resp, err) -> {
										if (resp != null) {
											CommonJobSingleton.getInstance().addRecordData(
												finalBatchCount,
												finalTotalSize,
												System.currentTimeMillis() - startTime);
										} else {
											LOGGER.error("Exception while sending {} to Kinesis.", trcFile.getFileName());
											LOGGER.error(ExceptionUtils.getExceptionStackTrace(new Exception(err)));
										}
									});
								batchCount = 0;
								totalSize = 0;
							}
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
						byte[] messageData = null;
						if (value.getBytes().length > fileSizeThreshold) {
							messageData = GzipUtils.compress(value);
						} else {
							messageData = value.getBytes();
						}
						final int dataLength = messageData.length;
						PutRecordRequest putRecordRequest  = PutRecordRequest.builder()
								.streamName(streamName)
								.partitionKey(trcFile.getFileName())
								.data(SdkBytes.fromByteArray(messageData))
								.build();
						kinesisClient.putRecord(putRecordRequest)
							.whenComplete((resp, err) -> {
								if (resp != null) {
									CommonJobSingleton.getInstance().addRecordData(
											dataLength,
											System.currentTimeMillis() - startTime);
								} else {
									LOGGER.error("Exception while sending {} to Kinesis.", trcFile.getFileName());
									LOGGER.error(ExceptionUtils.getExceptionStackTrace(new Exception(err)));
								}
						});
					}
				} else {
					LOGGER.error("Mailformed or incomplete xml file: {}", trcFile.getFileName());
					if (lockedFiles.contains(trcFile)) {
						LOGGER.error("Unhanled concurrency issue with xml file: {}", trcFile.getFileName());
					} else {
						LOGGER.error("Adding {} to queue again", trcFile.getFileName());
					}
				}
			} catch (IOException jpe) {
				LOGGER.error(ExceptionUtils.getExceptionStackTrace(jpe));
			}
		};
		return task;
	}

	public void shutdown() {
		if (kinesisClient != null) {
			kinesisClient.close();
		} else {
			LOGGER.error("Attempt to close non-initialized Kinesis producer!");
			System.exit(1);
		}
	}

	public void parseSettings(final Properties props, final String configPath, final int exitCode) {
		streamName = props.getProperty("a2.kinesis.stream", "");
		if (streamName == null || "".equals(streamName)) {
			LOGGER.error("a2.kinesis.stream parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		String region = props.getProperty("a2.kinesis.region", "");
		if (region == null || "".equals(region)) {
			LOGGER.error("a2.kinesis.region parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		String accessKey = props.getProperty("a2.kinesis.access.key", "");
		if (accessKey == null || "".equals(accessKey)) {
			LOGGER.error("a2.kinesis.access.key parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		String accessSecret = props.getProperty("a2.kinesis.access.secret", "");
		if (accessSecret == null || "".equals(accessSecret)) {
			LOGGER.error("a2.kinesis.access.secret parameter must set in configuration file {}", configPath);
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		// The maxConnections parameter can be used to control the degree of
        // parallelism when making HTTP requests.
		int maxConnections = 50;
		String maxConnectionsString = props.getProperty("a2.kinesis.max.connections", "");
		if (maxConnectionsString != null && !"".equals(maxConnectionsString)) {
			try {
				maxConnections = Integer.parseInt(maxConnectionsString);
			} catch (Exception e) {
				LOGGER.warn("Incorrect value for a2.kinesis.max.connections -> {}", maxConnectionsString);
				LOGGER.warn("Setting it to 50");
			}
		}

		// Request timeout milliseconds
		long requestTimeout = 30000;
		String requestTimeoutString = props.getProperty("a2.kinesis.request.timeout", "");
		if (requestTimeoutString != null && !"".equals(requestTimeoutString)) {
			try {
				requestTimeout = Integer.parseInt(requestTimeoutString);
			} catch (Exception e) {
				LOGGER.warn("Incorrect value for a2.kinesis.request.timeout -> {}", requestTimeoutString);
				LOGGER.warn("Setting it to 30000");
			}
		}

		AwsCredentials awsCreds = AwsBasicCredentials.create(accessKey, accessSecret);
		AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCreds);

		kinesisClient = KinesisAsyncClient.builder()
				.credentialsProvider(credentialsProvider)
				.region(Region.of(region))
				.httpClientBuilder(NettyNioAsyncHttpClient.builder()
						.maxConcurrency(maxConnections)
						.maxPendingConnectionAcquires(10_000)
						.writeTimeout(Duration.ofMillis(requestTimeout))
						.readTimeout(Duration.ofMillis(requestTimeout)))
				.build();

		// fileSizeThreshold
		String fileSizeThresholdString = props.getProperty("a2.kinesis.file.size.threshold", "");
		if (fileSizeThresholdString != null && !"".equals(fileSizeThresholdString)) {
			try {
				fileSizeThreshold = Integer.parseInt(fileSizeThresholdString);
			} catch (Exception e) {
				LOGGER.warn("Incorrect value for a2.kinesis.file.size.threshold -> {}", fileSizeThresholdString);
				LOGGER.warn("Setting it to 512");
			}
		}
	}

}
