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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.OffsetDateTime;

public class TypeUtils {

	/**
	 * Converts java.time.OffsetDateTime to java.sql.Timestamp
	 * @param
	 * @return
	 **/
	public static Timestamp odt2Timestamp(OffsetDateTime odt) {
		if (odt == null)
			return null;
		else {
			Timestamp ts = new Timestamp(odt.toInstant().toEpochMilli());
			ts.setNanos(odt.getNano());
			return ts;
		}
	}

	/**
	 * Converts java.math.BigInteger to java.math.BigDecimal
	 * @param
	 * @return
	 **/
	public static BigDecimal int2Dec(BigInteger bi) {
		if (bi == null)
			return null;
		else
			return new BigDecimal(bi);
	}

}
