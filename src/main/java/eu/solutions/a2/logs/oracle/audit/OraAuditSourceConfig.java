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

import java.util.Map;

import org.apache.kafka.common.config.AbstractConfig;
import org.apache.kafka.common.config.ConfigDef;
import org.apache.kafka.common.config.ConfigDef.Importance;
import org.apache.kafka.common.config.ConfigDef.Type;

import eu.solutions.a2.logs.Constants;

public class OraAuditSourceConfig extends AbstractConfig {

	public static final String TASK_PARAM_TOPIC = "topic";
	public static final String TASK_PARAM_POLL_MS = "poll.ms";

	public static ConfigDef config() {
		return new ConfigDef()
				.define(Constants.PARAM_A2_WATCHED_PATH, Type.STRING, Importance.HIGH, Constants.PARAM_A2_WATCHED_PATH_DOC)
				.define(Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL,
						Type.INT,
						Integer.toString(Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL_DEFAULT),
						Importance.HIGH,
						Constants.PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL_DOC)
				.define(Constants.PARAM_A2_DATA_FORMAT,
						Type.STRING,
						Constants.PARAM_A2_DATA_FORMAT_JSON,
						ConfigDef.ValidString.in(Constants.PARAM_A2_DATA_FORMAT_RAW, Constants.PARAM_A2_DATA_FORMAT_JSON),
						Importance.HIGH,
						Constants.PARAM_A2_DATA_FORMAT_DOC)
				.define(Constants.PARAM_A2_TARGET_TOPIC,
						Type.STRING,
						Importance.HIGH,
						Constants.PARAM_A2_TARGET_TOPIC_DOC)
				.define(Constants.PARAM_A2_POLL_INTERVAL,
						Type.INT,
						Integer.toString(Constants.PARAM_A2_POLL_INTERVAL_DEFAULT),
						Importance.HIGH,
						Constants.PARAM_A2_POLL_INTERVAL_DOC);
	}

	public OraAuditSourceConfig(Map<?, ?> originals) {
		super(config(), originals);
	}

}
