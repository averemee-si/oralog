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

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Oracle Database Audit Structure
 * based on:
 *  http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd 
 *  http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-12_2.xsd
 * 
 */
@JacksonXmlRootElement(localName = "Audit")
@JsonPropertyOrder({"version", "auditRecord"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class OraAuditPojo {

	@JsonProperty(value = "version", required = true)
	@JacksonXmlProperty(localName = "Version", isAttribute = false)
    private String version;
	@JsonProperty(value = "auditRecord", required = false)
	@JacksonXmlProperty(localName = "AuditRecord", isAttribute = false)
	@JacksonXmlElementWrapper(useWrapping = false)
    private List<OraAuditRecordPojo> auditRecord;

	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	public List<OraAuditRecordPojo> getAuditRecord() {
		return auditRecord;
	}
	public void setAuditRecord(List<OraAuditRecordPojo> auditRecord) {
		this.auditRecord = auditRecord;
	}

}
