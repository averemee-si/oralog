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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.utils.ExceptionUtils;
import eu.solutions.a2.utils.Version;

public class OraAuditSourceConnector extends SourceConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAuditSourceConnector.class);
	private OraAuditSourceConfig config;
	private int dataFormat;

	@Override
	public void start(Map<String, String> props) {
		LOGGER.info("Starting oralog version {} for Oracle XML Audit files Source Connector...", version());
		config = new OraAuditSourceConfig(props);

		final String watchedPath = config.getString(Constants.PARAM_A2_WATCHED_PATH);
		// Sanity check - Check if path is a folder
		final Path watchedPathNio = Paths.get(watchedPath);
		try {
			Boolean isFolder = (Boolean) Files.getAttribute(watchedPathNio, "basic:isDirectory", LinkOption.NOFOLLOW_LINKS);
			if (!isFolder) {
				LOGGER.error("Path specified by {} parameter [{}] is not a folder", Constants.PARAM_A2_WATCHED_PATH, watchedPathNio);
				throw new RuntimeException(watchedPath + " is not a folder!");
			}
		} catch (IOException ioe) {
			// Folder does not exists
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(ioe));
			LOGGER.error("Path specified by {} parameter [{}] not exist!", Constants.PARAM_A2_WATCHED_PATH, watchedPathNio);
			throw new RuntimeException(watchedPath + " does not exist!");
		}

		OraAuditSourceXmlFilesSingleton filesProcessor = OraAuditSourceXmlFilesSingleton.getInstance();
		filesProcessor.start(
				config.getInt(Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL),
				watchedPathNio);

		if (Constants.PARAM_A2_DATA_FORMAT_RAW.equalsIgnoreCase(
				config.getString(Constants.PARAM_A2_DATA_FORMAT))) {
			dataFormat = Constants.DATA_FORMAT_RAW_STRING;
		} else if (Constants.PARAM_A2_DATA_FORMAT_JSON.equalsIgnoreCase(
				config.getString(Constants.PARAM_A2_DATA_FORMAT))) {
			dataFormat = Constants.DATA_FORMAT_JSON;
		}

		//TODO
		//TODO more???
		//TODO
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		final List<Map<String, String>> configs = new ArrayList<>(maxTasks);
		for (int i = 0; i < maxTasks; i++) {

			final Map<String, String> taskParam = new HashMap<>(3);
			taskParam.put(Constants.PARAM_A2_DATA_FORMAT, Integer.toString(dataFormat));
			taskParam.put(OraAuditSourceConfig.TASK_PARAM_POLL_MS, Integer.toString(config.getInt(Constants.PARAM_A2_POLL_INTERVAL)));
			taskParam.put(OraAuditSourceConfig.TASK_PARAM_TOPIC, config.getString(Constants.PARAM_A2_TARGET_TOPIC));
			configs.add(taskParam);
		}
		return configs;
	}

	@Override
	public void stop() {
		OraAuditSourceXmlFilesSingleton filesProcessor = OraAuditSourceXmlFilesSingleton.getInstance();
		filesProcessor.shutdown();
	}

	@Override
	public Class<? extends Task> taskClass() {
		return OraAuditSourceTask.class;
	}

	@Override
	public ConfigDef config() {
		return OraAuditSourceConfig.config();
	}

	@Override
	public String version() {
		return Version.getVersion();
	}

}
