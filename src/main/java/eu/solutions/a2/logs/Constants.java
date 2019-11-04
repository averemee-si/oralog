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

package eu.solutions.a2.logs;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;

public class Constants {

	/** Supported data formats */
	public static final int DATA_FORMAT_RAW_STRING = 0;
	public static final int DATA_FORMAT_JSON = 1;

	/** Common parameter definitions */
	public static final String PARAM_A2_FILE_QUERY_INTERVAL = "a2.file.query.interval";
	public static final String PARAM_A2_FILE_QUERY_INTERVAL_DOC = "Interval in milliseconds between checks for new lines in file";
	public static final int PARAM_A2_FILE_QUERY_INTERVAL_DEFAULT = 1000;
	
	public static final String PARAM_A2_DATA_FORMAT = "a2.data.format";
	public static final String PARAM_A2_DATA_FORMAT_DOC = "Format to send data: RAW as read from file or convert it to JSON";
	public static final String PARAM_A2_DATA_FORMAT_RAW = "raw";
	public static final String PARAM_A2_DATA_FORMAT_JSON = "json";

	public static final String PARAM_A2_WATCHED_PATH = "a2.watched.path";
	public static final String PARAM_A2_WATCHED_PATH_DOC = "Path for Oracle Database audit files. Must equal AUDIT_FILE_DEST database parameter";

	public static final String PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL = "a2.locked.file.query.interval";
	public static final String PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL_DOC = "Interval in milliseconds between checks for 'file in use' - i.e. end of writing to file";
	public static final int PARAM_A2_LOCKED_QRY_RFRSH_INTERVAL_DEFAULT = 1000;

	public static final String PARAM_A2_TARGET_TOPIC = "a2.target.topic";
	public static final String PARAM_A2_TARGET_TOPIC_DOC = "Destination topic name";

	public static final String PARAM_A2_POLL_INTERVAL = "a2.poll.interval";
	public static final String PARAM_A2_POLL_INTERVAL_DOC = "Frequency in milliseconds to poll for new data for each task";
	public static final int PARAM_A2_POLL_INTERVAL_DEFAULT = 100;

	public static final Schema SCHEMA_RAW = SchemaBuilder
			.struct()
			.name("raw_data")
			.version(1)
				.field("raw_data", Schema.STRING_SCHEMA)
			.build();

}
