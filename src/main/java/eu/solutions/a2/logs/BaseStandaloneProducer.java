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

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.solutions.a2.standalone.CommonJobSingleton;
import eu.solutions.a2.standalone.KafkaSingleton;
import eu.solutions.a2.standalone.KinesisSingleton;
import eu.solutions.a2.standalone.SendMethodIntf;
import eu.solutions.a2.utils.ExceptionUtils;


public abstract class BaseStandaloneProducer  {

	private static final Logger LOGGER = LoggerFactory.getLogger(BaseStandaloneProducer.class);

	protected static final Properties props = new Properties();
	/** Supported target systems */
	protected static final int TARGET_KAFKA = 0;
	protected static final int TARGET_KINESIS = 1;
	protected static final String TARGET_NAME_KAFKA = "kafka";
	protected static final String TARGET_NAME_KINESIS = "kinesis";
	protected static final String TARGET_BROKER_PARAM = "a2.target.broker";
	/** Set default target system to Apache Kafka */
	protected static int targetSystem = TARGET_KAFKA;
	/** Data format */
	protected static int dataFormat = Constants.DATA_FORMAT_JSON;
	/** Target broker interface */
	protected static SendMethodIntf sendMethod = null;


	protected static void initLog4j(int exitCode) {
		// Perform basic log4j configuration
    	BasicConfigurator.configure();
		// Check for valid log4j configuration
		String log4jConfig = System.getProperty("a2.log4j.configuration");
		if (log4jConfig == null || "".equals(log4jConfig)) {
			System.err.println("JVM argument -Da2.log4j.configuration must set!");
			System.err.println("Exiting.");
			System.exit(exitCode);
		}

		// Check that log4j configuration file exist
		Path path = Paths.get(log4jConfig);
		if (!Files.exists(path) || Files.isDirectory(path)) {
			System.err.println("JVM argument -Da2.log4j.configuration points to unknown file " + log4jConfig + "!");
			System.err.println("Exiting.");
			System.exit(exitCode);
		}
		// Initialize log4j
		PropertyConfigurator.configure(log4jConfig);

	}

	protected static void printUsage(String className, int exitCode) {
		LOGGER.error("Usage:\njava {} <full path to configuration file>", className);
		LOGGER.error("Exiting.");
		System.exit(exitCode);
	}

	protected static void loadPropsAndParseCommonSettings(final String configPath, int exitCode) {
		try {
			props.load(new FileInputStream(configPath));
		} catch (IOException eoe) {
			LOGGER.error("Unable to open configuration file {}.", configPath);
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(eoe));
			LOGGER.error("Exiting.");
			System.exit(exitCode);
		}

		final String targetBroker = props.getProperty(TARGET_BROKER_PARAM, TARGET_NAME_KAFKA).trim();
		if (TARGET_NAME_KAFKA.equalsIgnoreCase(targetBroker)) {
			targetSystem = TARGET_KAFKA;
		} else if (TARGET_NAME_KINESIS.equalsIgnoreCase(targetBroker)) {
			targetSystem = TARGET_KINESIS;
		} else {
			LOGGER.warn("Wrong target broker type '{}' specified in configuration file {}", targetBroker, configPath);
			LOGGER.warn("Setting target broker type to kafka");
		}
		// Set default data format to Raw string
		final String dataFormatString = props.getProperty(Constants.PARAM_A2_DATA_FORMAT);
		if (dataFormatString != null && !"".equals(dataFormatString)) {
			if (Constants.PARAM_A2_DATA_FORMAT_RAW.equalsIgnoreCase(dataFormatString)) {
				dataFormat = Constants.DATA_FORMAT_RAW_STRING;
			} else if (Constants.PARAM_A2_DATA_FORMAT_JSON.equalsIgnoreCase(dataFormatString)) {
				dataFormat = Constants.DATA_FORMAT_JSON;
			} else {
				LOGGER.warn("Incorrect value for {} -> {}", Constants.PARAM_A2_DATA_FORMAT, dataFormatString);
				LOGGER.warn("Setting it to -> {}", Constants.PARAM_A2_DATA_FORMAT_JSON);
			}
		}

		//TODO - different MBeans!!!!!
		// Init CommonJob MBean
        CommonJobSingleton.getInstance();

		if (targetSystem == TARGET_KAFKA) {
			sendMethod = KafkaSingleton.getInstance(); 
		} else if (targetSystem == TARGET_KINESIS) {
			sendMethod = KinesisSingleton.getInstance(); 
		}
		sendMethod.parseSettings(props, configPath, 3);
		// Set message format
		sendMethod.setMessageFormat(dataFormat);


	}

}
