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

package eu.solutions.a2.logs.oracle.adr;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class UnmarshallerTest {

	@Test
	public void test() {
		final String testCase1 = 
				"<msg time='2019-05-02T16:52:30.837+02:00' org_id='oracle' comp_id='rdbms'\n" +
				" msg_id='opistr_real:1184:2538814769' type='NOTIFICATION' group='startup'\n" +
				" level='16' host_id='kafka.a2-solutions.eu' host_addr='172.16.214.138'\n" +
				" pid='7720' version='1'>\n" +
				" <txt>Starting ORACLE instance (normal) (OS id: 7720)\n" +
				" </txt>\n" +
				"</msg>\n";
		final String testCase2 =
				"<msg time='2019-07-04T21:23:54.788+02:00' org_id='oracle' comp_id='rdbms'\n" +
				" msg_id='2633049240' type='INCIDENT_ERROR' level='1'\n" +
				" host_id='kafka.a2-solutions.eu' host_addr='172.16.214.138' pid='7760'\n" +
				" prob_key='ORA 700 [kskvmstatact: excessive swapping observed]' downstream_comp='VOS' errid='51730'\n" +
				" detail_path='/oracle/admin/diag/rdbms/jdk8/JDK8/trace/JDK8_dbrm_7760.trc'>\n" +
				" <txt>Errors in file /oracle/admin/diag/rdbms/jdk8/JDK8/trace/JDK8_dbrm_7760.trc  (incident=51730):\n" +
				"ORA-00700: soft internal error, arguments: [kskvmstatact: excessive swapping observed], [], [], [], [], [], [], [], [], [], [], []\n" +
				" </txt>\n" +
				"</msg>";
		final String testCase3 =
				"<msg time='2019-07-04T21:42:19.079+02:00' org_id='oracle' comp_id='rdbms'\n" +
				" type='UNKNOWN' level='16' host_id='kafka.a2-solutions.eu'\n" +
				" host_addr='172.16.214.138' module='sqlplus@kafka.a2-solutions.eu (TNS V1-V3)' pid='8273'>\n" +
				" <txt> Thread 1: RBA 113.85201.16, nab 85201, scn 0x00000000001d17fb\n" +
				" </txt>" +
				"</msg>";
		try {
			ObjectReader or = new XmlMapper()
					.registerModule(new JavaTimeModule())
					.readerFor(OraAdrPojo.class);
			OraAdrPojo oap = null;
			oap = or.readValue(testCase1);
			System.out.println(oap.getTime());
			oap = or.readValue(testCase2);
			System.out.println(oap.getTime());
			oap = or.readValue(testCase3);
			System.out.println(oap.getTime());
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}
	}
}
