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

package eu.solutions.a2.utils;

import java.io.IOException;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(OsUtils.class);

	/**
	 * Executes OS command and returns it output.
	 * @param command OS command
	 * @return String with the output of OS command
	 **/
	public static String execAndGetResult(String command) {
		String result = null;
		try (Scanner scanner = new Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A")) {
			result = scanner.hasNext() ? scanner.next() : "";
		} catch (IOException e) {
			LOGGER.error("Can't execute OS command:" + command);
			LOGGER.error(ExceptionUtils.getExceptionStackTrace(e));
		}
		return result;
	}


}
