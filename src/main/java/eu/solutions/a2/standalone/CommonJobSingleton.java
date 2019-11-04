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

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.apache.log4j.Logger;

public class CommonJobSingleton {

	private static final Logger LOGGER = Logger.getLogger(CommonJobSingleton.class);

	private static CommonJobSingleton instance;

	/** MBean */
	private final CommonJobMgmt mbean;

	private CommonJobSingleton() {
		mbean = new CommonJobMgmt();
		try {
			ObjectName name = new ObjectName("eu.solutions.a2.oracdc:type=CommonJobMgmt,name=oralog");
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			mbs.registerMBean(mbean, name);
		} catch (MalformedObjectNameException e) {
			LOGGER.fatal("Unable to register MBean - mailformed object!!!");
			LOGGER.fatal("Exiting");
			System.exit(1);
		} catch (InstanceAlreadyExistsException e) {
			LOGGER.fatal("Unable to register MBean - instance already exists!!!");
			LOGGER.fatal("Exiting");
			System.exit(1);
		} catch (MBeanRegistrationException e) {
			LOGGER.fatal("Unable to register MBean - registration exception!!!");
			LOGGER.fatal("Exiting");
			System.exit(1);
		} catch (NotCompliantMBeanException e) {
			LOGGER.fatal("Unable to register MBean - not compliant MBean!!!");
			LOGGER.fatal("Exiting");
			System.exit(1);
		}
	}

	public static CommonJobSingleton getInstance() {
		if (instance == null) {
			instance = new CommonJobSingleton();
		}
		return instance;
	}

	public void addRecordData(final long recordSize, final long elapsedMillis) {
		mbean.addRecordData(recordSize, elapsedMillis);
	}

	public void addRecordData(final long recordNo, final long recordSize, final long elapsedMillis) {
		mbean.addRecordData(recordNo, recordSize, elapsedMillis);
	}

	public void setTableCount(int tableCount) {
//		mbean.setTableCount(tableCount);
	}

	public long getProcessedRecordCount() {
		return mbean.getRecordCount();
	}

}
