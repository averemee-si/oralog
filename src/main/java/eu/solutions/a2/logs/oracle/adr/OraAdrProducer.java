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

package eu.solutions.a2.logs.oracle.adr;

import java.io.File;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.logs.BaseStandaloneProducer;
import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.utils.ExceptionUtils;


public class OraAdrProducer extends BaseStandaloneProducer  {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAdrProducer.class);

	/** Prefix for watched file parameter(s) */
	private static final String WATCHED_FILE_PREFIX = "a2.watched.adr.file";
	/**   Main thread pool for data tansfer jobs */
	private static ThreadPoolExecutor threadPool;

    public static void main( String[] argv) {
    	initLog4j(1);
		if (argv.length == 0) {
			printUsage(OraAdrProducer.class.getCanonicalName(), 2);
		}
		// Load producer properties, detect target system, set data format
		loadPropsAndParseCommonSettings(argv[0], 1);

		int fileQueryInterval = Constants.PARAM_A2_FILE_QUERY_INTERVAL_DEFAULT;
		final String fileQueryIntervalString = props.getProperty(Constants.PARAM_A2_FILE_QUERY_INTERVAL);
		if (fileQueryIntervalString != null && !"".equals(fileQueryIntervalString)) {
			try {
				fileQueryInterval = Integer.parseInt(fileQueryIntervalString);
			} catch (Exception e) {
				LOGGER.warn("Incorrect value for {} -> {}", Constants.PARAM_A2_FILE_QUERY_INTERVAL, fileQueryIntervalString);
				LOGGER.warn("Setting it to {}", Constants.PARAM_A2_FILE_QUERY_INTERVAL_DEFAULT);
			}
		}
		// For use in Lambda
		final int fileQueryIntervalFinal = fileQueryInterval;
		String osName = System.getProperty("os.name").toUpperCase();
		LOGGER.info("Running on " + osName);

		List<AbstractMap.SimpleImmutableEntry<Thread, Tailer>> adrTailers = new ArrayList<>();
		props.forEach((key, value) -> {
			final String paramName = ((String)key).toLowerCase();
			if (paramName.startsWith(WATCHED_FILE_PREFIX)) {
				final String filePath = (String) value;
				File watchedFile = null;
				try {
					watchedFile = new File(filePath);
				} catch (Exception fnf) {
					LOGGER.error("{} points to nonexisting file {}", paramName, filePath);
					LOGGER.error("Exiting.");
					System.exit(3);
				}
				// Sanity check - must be file
				if (!watchedFile.isFile()) {
					LOGGER.error("{} must be file.", filePath);
					LOGGER.error("Exiting.");
					System.exit(3);
				}
				// Corresponding message prefix
				final String msgPrefixParam = "a2.watched.adr.message.prefix" + 
											paramName.substring(WATCHED_FILE_PREFIX.length());
				final String msgPrefix = (String) props.getOrDefault(msgPrefixParam, "adr-" + adrTailers.size());
				// Create listener and tailer
				AdrTailListener atl = new AdrTailListener(msgPrefix);
				Tailer tailer = new Tailer(watchedFile, atl, fileQueryIntervalFinal, true);
				LOGGER.info("Watching for " + filePath);
				LOGGER.info("Using prefix " + msgPrefix);
				// Corresponding thread
				Thread thread = new Thread(tailer);
				thread.setDaemon(true);
				thread.setName("Thread-" + msgPrefix);

				adrTailers.add(new AbstractMap.SimpleImmutableEntry<Thread, Tailer>(
								thread, tailer));
			}
		});

		// Add special shutdown thread
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LOGGER.info("Shutting down...");
				adrTailers.forEach((entry) -> {
					// Stop tailer
					entry.getValue().stop();
				});
				sendMethod.shutdown();
				//TODO - more information about processing
			}
		});

		// Initialize main thread pool
		BlockingQueue<Runnable> blockingQueue = new ArrayBlockingQueue<Runnable>(4096 * adrTailers.size());
		threadPool = new ThreadPoolExecutor(
				adrTailers.size(),	// core pool size
				adrTailers.size(),	// maximum pool size
				15,	// If the pool currently has more than corePoolSize threads, excess threads will be terminated if they have been idle for more than the keepAliveTime
				TimeUnit.SECONDS,	// Time unit for keep-alive
				blockingQueue	// Job queue
				);
		// Throw RejectedExecutionException with full queue
		threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

		// Start watching
		adrTailers.forEach((entry) -> {
			Thread thread = entry.getKey();
			LOGGER.info("Starting thread " + thread.getName());
			thread.start();
		});
		while (true) {
			try {
				Thread.sleep(fileQueryInterval * 16);
			} catch (Exception e) {}
		}
    }

	private static class AdrTailListener extends TailerListenerAdapter {

		final private String prefix;

		private StringBuilder msg = null;
		private boolean msgFlag = false;
		private long msgCount = 0;

		AdrTailListener(String prefix) {
			this.prefix = prefix;
		}

		@Override
		public void handle(final String line) {
			final String trimmedLine = line.trim();
			if (trimmedLine.startsWith("<msg")) {
				msgFlag = true;
				msg = new StringBuilder(1024);
				msg.append(line);
			} else if (trimmedLine.endsWith("</msg>")) {
				if (msgFlag) {
					msgFlag = false;
					msg.append(line);

					final StringBuilder messageKey = new StringBuilder(32);
					messageKey.append(prefix);
					messageKey.append(":");
					messageKey.append(msgCount++);
					messageKey.append(":");
					messageKey.append(System.currentTimeMillis());

					try {
						threadPool.submit(sendMethod.oraAdrJob(messageKey.toString(), msg.toString()));
					} catch (RejectedExecutionException ree) {
						LOGGER.error("Can't send message to Kafka!");
						LOGGER.error(ExceptionUtils.getExceptionStackTrace(ree));
					}
				} else {
					LOGGER.error("Message ends without start tag!");
					LOGGER.error("Bad data = {}", line);
				}
			} else if (msgFlag) {
				msg.append(line);
				msg.append("\n");
			} else {
				LOGGER.error("Unrecognized Oracle ADR file xml format!");
				LOGGER.error("Bad data = {}", line);
			}
		}
	}

}
