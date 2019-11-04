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

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OraAuditTraceNameHolder {

	private static final Logger LOGGER = LoggerFactory.getLogger(OraAuditTraceNameHolder.class);

	private final String fileName;
	private String pid;

	protected OraAuditTraceNameHolder(final String fileName) {
		this.fileName = fileName;
		try {
//			String[] parts = fileName.split("_");
//			this.pid = parts[parts.length - 2];
//			this.timeStamp = parts[parts.length - 1].substring(0, parts[parts.length - 1].lastIndexOf('.'));
//			parts = null;
			String part = fileName.substring(fileName.lastIndexOf(File.separator) + 1, fileName.lastIndexOf('_'));
			this.pid = part.substring(part.lastIndexOf('_') + 1);
		} catch( Exception e) {
			LOGGER.error("Exception while parsing file name {}!!!", fileName);
		}
	}

	public String getPid() {
		return pid;
	}

	public String getFileName() {
		return fileName;
	}

}
