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

import java.time.OffsetDateTime;

import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import eu.solutions.a2.utils.TypeUtils;

/**
 * 
 * 
 * 
 */
@JacksonXmlRootElement(localName = "msg")
public class OraAdrPojo {


	@JsonProperty("MESSAGE_TEXT")
	@JacksonXmlProperty(localName = "txt", isAttribute = false)
    private String txt;
	@JsonProperty("ORIGINATING_TIMESTAMP")
	@JacksonXmlProperty(localName = "time", isAttribute = true)
    private OffsetDateTime time;
	@JsonProperty("NORMALIZED_TIMESTAMP")
	@JacksonXmlProperty(localName = "time_norm", isAttribute = true)
    private OffsetDateTime timeNorm;
	@JsonProperty("ORGANIZATION_ID")
	@JacksonXmlProperty(localName = "org_id", isAttribute = true)
    private String orgId;
	@JsonProperty("COMPONENT_ID")
	@JacksonXmlProperty(localName = "comp_id", isAttribute = true)
    private String compId;
	@JsonProperty("HOST_ID")
	@JacksonXmlProperty(localName = "host_id", isAttribute = true)
    private String hostId;
	@JsonProperty("HOST_ADDRESS")
	@JacksonXmlProperty(localName = "host_addr", isAttribute = true)
    private String hostAddr;
	@JsonProperty("MESSAGE_TYPE")
	@JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;
	@JsonProperty("MESSAGE_LEVEL")
	@JacksonXmlProperty(localName = "level", isAttribute = true)
    private Long level;
	@JsonProperty("MESSAGE_ID")
	@JacksonXmlProperty(localName = "msg_id", isAttribute = true)
    private String msgId;
	@JsonProperty("MESSAGE_GROUP")
	@JacksonXmlProperty(localName = "group", isAttribute = true)
	private String group;
	@JsonProperty("CLIENT_ID")
	@JacksonXmlProperty(localName = "client_id", isAttribute = true)
	private String clientId;
	@JsonProperty("MODULE_ID")
	@JacksonXmlProperty(localName = "module", isAttribute = true)
	private String module;
	@JsonProperty("PROCESS_ID")
	@JacksonXmlProperty(localName = "pid", isAttribute = true)
	private String pid;
	@JsonProperty("THREAD_ID")
	@JacksonXmlProperty(localName = "tid", isAttribute = true)
	private String tid;
	@JsonProperty("USER_ID")
	@JacksonXmlProperty(localName = "user", isAttribute = true)
	private String user;
	@JsonProperty("INSTANCE_ID")
	@JacksonXmlProperty(localName = "inst_id", isAttribute = true)
	private Short instId;
	@JsonProperty("DETAILED_LOCATION")
	@JacksonXmlProperty(localName = "detail_path", isAttribute = true)
	private String detailPath;
	@JsonProperty("PROBLEM_KEY")
	@JacksonXmlProperty(localName = "prob_key", isAttribute = true)
	private String probKey;
	@JsonProperty("UPSTREAM_COMP_ID")
	@JacksonXmlProperty(localName = "upstream_comp", isAttribute = true)
	private String upstreamComp;
	@JsonProperty("DOWNSTREAM_COMP_ID")
	@JacksonXmlProperty(localName = "downstream_comp", isAttribute = true)
	private String downstreamComp;
	@JsonProperty("EXECUTION_CONTEXT_ID")
	@JacksonXmlProperty(localName = "ecid", isAttribute = true)
	private String ecid;
	@JsonProperty("VERSION")
	@JacksonXmlProperty(localName = "version", isAttribute = true)
	private Short version;
	//TODO
	@JacksonXmlProperty(localName = "errid", isAttribute = true)
	private Integer errid;
	//TODO
	@JacksonXmlProperty(localName = "rid", isAttribute = true)
	private Integer rid;
	//TODO
	@JacksonXmlProperty(localName = "err_seq", isAttribute = true)
	private Integer errSeq;

	private static final Schema SCHEMA_JSON = SchemaBuilder
		.struct()
		.name("oraadr")
		.version(1)
			.field("MESSAGE_TEXT", Schema.OPTIONAL_STRING_SCHEMA)
			.field("ORIGINATING_TIMESTAMP", Timestamp.builder().optional().build())
			.field("NORMALIZED_TIMESTAMP", Timestamp.builder().optional().build())
			.field("ORGANIZATION_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("COMPONENT_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("HOST_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("HOST_ADDRESS", Schema.OPTIONAL_STRING_SCHEMA)
			.field("MESSAGE_TYPE", Schema.OPTIONAL_STRING_SCHEMA)
			.field("MESSAGE_LEVEL", Schema.OPTIONAL_INT64_SCHEMA)
			.field("MESSAGE_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("MESSAGE_GROUP", Schema.OPTIONAL_STRING_SCHEMA)
			.field("CLIENT_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("MODULE_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("PROCESS_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("THREAD_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("USER_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("INSTANCE_ID", Schema.OPTIONAL_INT16_SCHEMA)
			.field("DETAILED_LOCATION", Schema.OPTIONAL_STRING_SCHEMA)
			.field("PROBLEM_KEY", Schema.OPTIONAL_STRING_SCHEMA)
			.field("UPSTREAM_COMP_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("DOWNSTREAM_COMP_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("EXECUTION_CONTEXT_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("VERSION", Schema.OPTIONAL_INT16_SCHEMA)
			//TODO
			.field("errid", Schema.OPTIONAL_INT32_SCHEMA)
			//TODO
			.field("rid", Schema.OPTIONAL_INT32_SCHEMA)
			//TODO
			.field("err_seq", Schema.OPTIONAL_INT32_SCHEMA)
		.build();

	@JsonIgnore
	public SourceRecord sourceRecord(final String topic) {
		final Struct struct = new Struct(SCHEMA_JSON);
		struct.put("MESSAGE_TEXT", this.txt);
		struct.put("ORIGINATING_TIMESTAMP", TypeUtils.odt2Timestamp(this.time));
		struct.put("NORMALIZED_TIMESTAMP", TypeUtils.odt2Timestamp(this.timeNorm));
		struct.put("ORGANIZATION_ID", this.orgId);
		struct.put("COMPONENT_ID", this.compId);
		struct.put("HOST_ID", this.hostId);
		struct.put("HOST_ADDRESS", this.hostAddr);
		struct.put("MESSAGE_TYPE", this.type);
		struct.put("MESSAGE_LEVEL", this.level);
		struct.put("MESSAGE_ID", this.msgId);
		struct.put("MESSAGE_GROUP", this.group);
		struct.put("CLIENT_ID", this.clientId);
		struct.put("MODULE_ID", this.module);
		struct.put("PROCESS_ID", this.pid);
		struct.put("THREAD_ID", this.tid);
		struct.put("USER_ID", this.user);
		struct.put("INSTANCE_ID", this.instId);
		struct.put("DETAILED_LOCATION", this.detailPath);
		struct.put("PROBLEM_KEY", this.probKey);
		struct.put("UPSTREAM_COMP_ID", this.upstreamComp);
		struct.put("DOWNSTREAM_COMP_ID", this.downstreamComp);
		struct.put("EXECUTION_CONTEXT_ID", this.ecid);
		struct.put("VERSION", this.version);
		//TODO
		struct.put("errid", this.errid);
		//TODO
		struct.put("rid", this.rid);
		//TODO
		struct.put("err_seq", this.errSeq);
		return new SourceRecord(null, null, topic, SCHEMA_JSON, struct);
	}

    public String getTxt() {
        return txt;
    }
    public void setTxt(String value) {
        this.txt = value;
    }

    public OffsetDateTime getTime() {
        return time;
    }
    public void setTime(OffsetDateTime value) {
        this.time = value;
    }

    public OffsetDateTime getTimeNorm() {
        return timeNorm;
    }
    public void setTimeNorm(OffsetDateTime value) {
        this.timeNorm = value;
    }

    public String getOrgId() {
        return orgId;
    }
    public void setOrgId(String value) {
        this.orgId = value;
    }

    public String getCompId() {
        return compId;
    }
    public void setCompId(String value) {
        this.compId = value;
    }

    public String getHostId() {
        return hostId;
    }
    public void setHostId(String value) {
        this.hostId = value;
    }

    public String getHostAddr() {
        return hostAddr;
    }
    public void setHostAddr(String value) {
        this.hostAddr = value;
    }

    public String getType() {
        return type;
    }
    public void setType(String value) {
        this.type = value;
    }

    public Long getLevel() {
        return level;
    }
    public void setLevel(Long value) {
        this.level = value;
    }

    public String getMsgId() {
        return msgId;
    }
    public void setMsgId(String value) {
        this.msgId = value;
    }

    public String getGroup() {
        return group;
    }
    public void setGroup(String value) {
        this.group = value;
    }

    public String getClientId() {
        return clientId;
    }
    public void setClientId(String value) {
        this.clientId = value;
    }

    public String getModule() {
        return module;
    }
    public void setModule(String value) {
        this.module = value;
    }

    public String getPid() {
        return pid;
    }
    public void setPid(String value) {
        this.pid = value;
    }

    public String getTid() {
        return tid;
    }
    public void setTid(String value) {
        this.tid = value;
    }

    public String getUser() {
        return user;
    }
    public void setUser(String value) {
        this.user = value;
    }

    public Short getInstId() {
        return instId;
    }
    public void setInstId(Short value) {
        this.instId = value;
    }

    public String getDetailPath() {
        return detailPath;
    }
    public void setDetailPath(String value) {
        this.detailPath = value;
    }

    public String getProbKey() {
        return probKey;
    }
    public void setProbKey(String value) {
        this.probKey = value;
    }

    public String getUpstreamComp() {
        return upstreamComp;
    }
    public void setUpstreamComp(String value) {
        this.upstreamComp = value;
    }

    public String getDownstreamComp() {
        return downstreamComp;
    }
    public void setDownstreamComp(String value) {
        this.downstreamComp = value;
    }

    public String getEcid() {
        return ecid;
    }
    public void setEcid(String value) {
        this.ecid = value;
    }

    public Integer getErrid() {
        return errid;
    }
    public void setErrid(Integer value) {
        this.errid = value;
    }

    public Integer getRid() {
        return rid;
    }
    public void setRid(Integer value) {
        this.rid = value;
    }

    public Integer getErrSeq() {
        return errSeq;
    }
    public void setErrSeq(Integer value) {
        this.errSeq = value;
    }

    public Short getVersion() {
        return version;
    }
    public void setVersion(Short value) {
        this.version = value;
    }

}
