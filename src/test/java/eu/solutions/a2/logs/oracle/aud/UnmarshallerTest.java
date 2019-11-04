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

package eu.solutions.a2.logs.oracle.aud;

import static org.junit.Assert.fail;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.solutions.a2.logs.oracle.audit.OraAuditPojo;

public class UnmarshallerTest {

	@Test
	public void test() {
		final String testCase =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"  <Audit xmlns=\"http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd\"\n" +
				"   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
				"   xsi:schemaLocation=\"http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd\">\n" +
				"   <Version>11.2</Version>\n" +
				"<AuditRecord><Audit_Type>4</Audit_Type><Session_Id>0</Session_Id><StatementId>0</StatementId><EntryId>1</EntryId><Extended_Timestamp>2019-08-12T11:36:44.455806Z</Extended_Timestamp><DB_User>/</DB_User><Userhost>apps.example.com</Userhost><OS_Process>28160</OS_Process><Terminal>UNKNOWN</Terminal><Instance_Number>0</Instance_Number><Returncode>0</Returncode><Scn>0</Scn><OSPrivilege>NONE</OSPrivilege><DBID>710804450</DBID>\n" +
				"<Sql_Text>select count(*), count(*), NULL from dba_hist_baseline_template</Sql_Text>\n" +
				"</AuditRecord>\n" +
				"<AuditRecord><Audit_Type>4</Audit_Type><Session_Id>0</Session_Id><StatementId>0</StatementId><EntryId>1879</EntryId><Extended_Timestamp>2019-08-12T11:37:34.739738Z</Extended_Timestamp><DB_User>/</DB_User><Userhost>apps.example.com</Userhost><OS_Process>28160</OS_Process><Terminal>UNKNOWN</Terminal><Instance_Number>0</Instance_Number><Returncode>0</Returncode><Scn>12204723579410</Scn><OSPrivilege>NONE</OSPrivilege><DBID>710804450</DBID>\n" +
				"<Sql_Text>select t.TEXT from SYS.VIEW$ t where t.rowid = :x</Sql_Text>\n" +
				"</AuditRecord>\n" +
				"</Audit>\n";
		
		try {
			ObjectReader or = new XmlMapper()
					.registerModule(new JavaTimeModule())
					.readerFor(OraAuditPojo.class);
			OraAuditPojo oap = or.readValue(testCase);
			System.out.println(oap.getVersion());
			System.out.println(oap.getAuditRecord().get(0).getExtendedTimestamp());
			System.out.println(oap.getAuditRecord().get(0).getSqlText().getValue());
			System.out.println(oap.getAuditRecord().get(1).getExtendedTimestamp());
			System.out.println(oap.getAuditRecord().get(1).getSqlText().getValue());
		} catch (IOException e) {
			e.printStackTrace();
			fail("IO Exception");
		}
	}
}
