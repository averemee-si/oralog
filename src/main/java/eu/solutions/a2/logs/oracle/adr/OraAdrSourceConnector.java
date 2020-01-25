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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.connect.connector.Task;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.utils.Version;

public class OraAdrSourceConnector extends SourceConnector {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAdrSourceConnector.class);
	private OraAdrSourceConfig config;
	private int fileQueryInterval;
	private int dataFormat;
	private boolean tailFromEnd;

	@Override
	public void start(Map<String, String> props) {
		LOGGER.info("Starting oralog version {} for Oracle ADR files Source Connector...", version());
		config = new OraAdrSourceConfig(props);

		fileQueryInterval  = config.getInt(Constants.PARAM_A2_FILE_QUERY_INTERVAL);
		if (Constants.PARAM_A2_DATA_FORMAT_RAW.equalsIgnoreCase(
				config.getString(Constants.PARAM_A2_DATA_FORMAT))) {
			dataFormat = Constants.DATA_FORMAT_RAW_STRING;
		} else if (Constants.PARAM_A2_DATA_FORMAT_JSON.equalsIgnoreCase(
				config.getString(Constants.PARAM_A2_DATA_FORMAT))) {
			dataFormat = Constants.DATA_FORMAT_JSON;
		}
		tailFromEnd = config.getBoolean(Constants.PARAM_A2_TAIL_FROM_END);
	}

	@Override
	public List<Map<String, String>> taskConfigs(int maxTasks) {
		List<String> filesToWatch = config.getList(OraAdrSourceConfig.PARAM_ADR_LOG_FILES);
		List<String> topicsToSend = config.getList(OraAdrSourceConfig.PARAM_TOPICS4FILES);
		String errorMessage = null;
		if (filesToWatch.size() == 0) {
			errorMessage = "No Oracle ADR files to watch specified!";
		} else if (filesToWatch.size() != topicsToSend.size()) {
			errorMessage = "Every file specified in "  + OraAdrSourceConfig.PARAM_ADR_LOG_FILES +
					" must have corresponding topic name in " + OraAdrSourceConfig.PARAM_TOPICS4FILES + "!";
		} else if (filesToWatch.size() != maxTasks) {
			errorMessage = "Parameter tasks.max must set to number of watched files (" + filesToWatch.size() + ")!";
		}
		if (errorMessage != null) {
			LOGGER.error(errorMessage);
			LOGGER.error("Stopping {}", OraAdrSourceConnector.class.getName());
			throw new ConnectException(errorMessage);
		}
		final List<Map<String, String>> configs = new ArrayList<>(maxTasks);
		for (int i = 0; i < maxTasks; i++) {
			File watchedFile = null;
			try {
				watchedFile = new File(filesToWatch.get(i));
			} catch (Exception fnf) {
				LOGGER.error("{} points to nonexisting file {}",
						OraAdrSourceConfig.PARAM_ADR_LOG_FILES, filesToWatch.get(i));
				LOGGER.error("Stopping {}", OraAdrSourceConnector.class.getName());
				throw new ConnectException(filesToWatch.get(i) + " not exist.");
			}
			// Sanity check - must be file
			if (!watchedFile.isFile()) {
				LOGGER.error("{} must be file.", filesToWatch.get(i));
				LOGGER.error("Stopping {}", OraAdrSourceConnector.class.getName());
				throw new ConnectException(filesToWatch.get(i) + " must be file.");
			}

			final Map<String, String> taskParam = new HashMap<>(5);
			taskParam.put(OraAdrSourceConfig.TASK_PARAM_FILE_PATH, filesToWatch.get(i));
			taskParam.put(OraAdrSourceConfig.TASK_PARAM_TOPIC, topicsToSend.get(i));
			taskParam.put(Constants.PARAM_A2_FILE_QUERY_INTERVAL, Integer.toString(fileQueryInterval));
			taskParam.put(Constants.PARAM_A2_DATA_FORMAT, Integer.toString(dataFormat));
			taskParam.put(Constants.PARAM_A2_TAIL_FROM_END, Boolean.toString(tailFromEnd));
			configs.add(taskParam);
		}
		return configs;
	}

	@Override
	public void stop() {
		// TODO Do we need more here?
	}

	@Override
	public Class<? extends Task> taskClass() {
		return OraAdrSourceTask.class;
	}

	@Override
	public ConfigDef config() {
		return OraAdrSourceConfig.config();
	}

	@Override
	public String version() {
		return Version.getVersion();
	}

}
