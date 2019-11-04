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

import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import eu.solutions.a2.logs.oracle.audit.OraAuditTraceNameHolder;

public interface SendMethodIntf {
	public void parseSettings(final Properties props, final String configPath, final int exitCode);
	public void shutdown();
	public void setMessageFormat(final int dataFormat);
	public Runnable oraAdrJob(final String key, final String envelope);
	public Runnable oraAudJob(
			final OraAuditTraceNameHolder trcFile,
			final ConcurrentLinkedQueue<OraAuditTraceNameHolder> lockedFiles);
}
