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

package eu.solutions.a2.logs.oracle.audit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class OraAuditSourceTask extends SourceTask {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAuditSourceTask.class);
	private static final ObjectReader reader = new XmlMapper()
			.registerModule(new JavaTimeModule())
			.readerFor(OraAuditPojo.class);

	private int pollTimeout;
	private int dataFormat;
	private String topic;

	@Override
	public void start(Map<String, String> props) {
		pollTimeout = Integer.parseInt(props.get(OraAuditSourceConfig.TASK_PARAM_POLL_MS));
		dataFormat = Integer.parseInt(props.get(Constants.PARAM_A2_DATA_FORMAT));
		topic = props.get(OraAuditSourceConfig.TASK_PARAM_TOPIC);
	}

	@Override
	public List<SourceRecord> poll() throws InterruptedException {
		synchronized (this) {
			this.wait(pollTimeout);
		}
		final List<SourceRecord> records = new ArrayList<>();

		final OraAuditTraceNameHolder trcFile =
				OraAuditSourceXmlFilesSingleton.getInstance().getReadyToProcess();
		if (trcFile != null) {
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
						OraAuditPojo oap = reader.readValue(value);
						for (OraAuditRecordPojo auditRecord : oap.getAuditRecord()) {
							records.add(auditRecord.sourceRecord(topic));
						}
					} else {
						// RAW file
						final Struct struct = new Struct(Constants.SCHEMA_RAW);
						struct.put("raw_data", value);
						records.add(new SourceRecord(null, null, topic, Constants.SCHEMA_RAW, struct));
					}
					LOGGER.info("Audit file {} processing time = {} ms", trcFile.getFileName(), System.currentTimeMillis() - startTime);
					Files.delete(auditFile.toPath());
				} else {
					LOGGER.error("Mailformed or incomplete xml file: {}", trcFile.getFileName());
					//TODO - what to do with this file???
				}
			} catch (IOException ioe) {
				LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
			}
		}
		return records;
	}

	@Override
	public void stop() {
		//TODO - more???
	}

	@Override
	public String version() {
		return Version.getVersion();
	}


}
