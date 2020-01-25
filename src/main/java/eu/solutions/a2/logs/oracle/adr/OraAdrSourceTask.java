/**
 * Copyright (c) 2018-present, A2 Rešitve d.o.o.
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.utils.ExceptionUtils;
import eu.solutions.a2.utils.Version;

public class OraAdrSourceTask extends SourceTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAdrSourceTask.class);
	private static final ObjectReader reader = new XmlMapper()
			.registerModule(new JavaTimeModule())
			.readerFor(OraAdrPojo.class);

	private int fileQueryInterval;
	private int dataFormat;
	private Tailer tailer;
	private final ConcurrentLinkedQueue<SourceRecord> queue = new ConcurrentLinkedQueue<>();

	@Override
	public void start(Map<String, String> props) {
		fileQueryInterval = Integer.parseInt(props.get(Constants.PARAM_A2_FILE_QUERY_INTERVAL));
		dataFormat = Integer.parseInt(props.get(Constants.PARAM_A2_DATA_FORMAT));
		final String filePath = props.get(OraAdrSourceConfig.TASK_PARAM_FILE_PATH);
		final boolean tailFromEnd = "true".equalsIgnoreCase(props.get(Constants.PARAM_A2_TAIL_FROM_END));
		AdrTailListener atl = new AdrTailListener(dataFormat, queue, props.get(OraAdrSourceConfig.TASK_PARAM_TOPIC));
		tailer = new Tailer(new File(filePath), atl, fileQueryInterval, tailFromEnd);
		Thread thread = new Thread(tailer);
		thread.setDaemon(true);
		thread.start();
		LOGGER.info("Watching for " + filePath);
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		synchronized (this) {
			this.wait(fileQueryInterval);
		}
		final List<SourceRecord> records = new ArrayList<>();
		SourceRecord sourceRecord = null;
		while ((sourceRecord = queue.poll()) != null) {
			records.add(sourceRecord);
		}
		return records;
	}

	@Override
	public void stop() {
		if (tailer != null) {
			tailer.stop();
		}
	}

	@Override
	public String version() {
		return Version.getVersion();
	}

	private static class AdrTailListener extends TailerListenerAdapter {

		private StringBuilder msg = null;
		private boolean msgFlag = false;
		private final int dataFormat;
		private final ConcurrentLinkedQueue<SourceRecord> queue;
		private final String topic;

		AdrTailListener(
				final int dataFormat, final ConcurrentLinkedQueue<SourceRecord> queue, final String topic) {
			this.dataFormat = dataFormat;
			this.queue = queue;
			this.topic = topic;
		}

		@Override
		public void handle(final String line) {
			final String trimmedLine = line.trim();
			if (trimmedLine.startsWith("<msg")) {
				msgFlag = true;
				msg = new StringBuilder(1024);
				msg.append(line);
				msg.append("\n");
			} else if (trimmedLine.endsWith("</msg>")) {
				if (msgFlag) {
					msgFlag = false;
					msg.append(line);

					if (dataFormat == Constants.DATA_FORMAT_RAW_STRING) {
						final Struct struct = new Struct(Constants.SCHEMA_RAW);
						struct.put("raw_data", msg.toString());
						queue.add(
								new SourceRecord(null, null, topic, Constants.SCHEMA_RAW, struct));
					} else if (dataFormat == Constants.DATA_FORMAT_JSON) {
						try {
							final OraAdrPojo oap = reader.readValue(msg.toString());
							queue.add(oap.sourceRecord(topic));
						} catch (IOException e) {
							LOGGER.error(ExceptionUtils.getExceptionStackTrace(e));
						}
					}
				} else {
					LOGGER.error("Message ends without start tag!");
					LOGGER.error("Bad data = {}", line);
				}
			} else if (msgFlag) {
				msg.append(line);
			} else {
				LOGGER.error("Unrecognized Oracle ADR file xml format!");
				LOGGER.error("Bad data = {}", line);
			}
		}
	}

}
