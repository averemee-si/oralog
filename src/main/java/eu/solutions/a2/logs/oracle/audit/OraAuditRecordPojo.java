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

import java.math.BigInteger;
import java.time.OffsetDateTime;

import org.apache.kafka.connect.data.Decimal;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Timestamp;
import org.apache.kafka.connect.source.SourceRecord;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText;

import eu.solutions.a2.utils.TypeUtils;

/**
 * Oracle Database Audit Record
 * based on:
 *  http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd 
 *  http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-12_2.xsd
 * JSON field names are in accordance with UNIFIED_AUDIT_TRAIL/DBA_AUDIT_TRAIL
 * 
 */
@JacksonXmlRootElement(localName = "AuditRecord")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({
	"auditType",
	"sessionId",
	"proxySessionId",
	"statementId",
	"entryId",
	"extendedTimestamp",
	"globalUid",
	"dbUser",
	"clientId",
	"extName",
	"osUser",
	"userhost",
	"osProcess",
	"terminal",
	"instanceNumber",
	"objectSchema",
	"objectName",
	"policyName",
	"newOwner",
	"newName",
	"action",
	"stmtType",
	"transactionId",
	"returncode",
	"scn",
	"commentText",
	"authPrivileges",
	"osPrivilege",
	"grantee",
	"privUsed",
	"sesActions",
	"objEditionName",
	"privGranted",
	"eContextId",
	"dbid",
	"sqlBind",
	"sqlText",
	"rlsInformation"
})
public class OraAuditRecordPojo {
	@JsonProperty(value = "AUDIT_TYPE", required = true)
	@JacksonXmlProperty(localName = "Audit_Type")
	private Short auditType;
	@JsonProperty("SESSIONID")
	@JacksonXmlProperty(localName = "Session_Id")
	private Long sessionId;
	@JsonProperty("PROXY_SESSIONID")
	@JacksonXmlProperty(localName = "Proxy_SessionId")
	private Long proxySessionId;
	@JsonProperty("STATEMENT_ID")
	@JacksonXmlProperty(localName = "StatementId")
	private BigInteger statementId;
	@JsonProperty("ENTRY_ID")
	@JacksonXmlProperty(localName = "EntryId")
	private BigInteger entryId;
	@JsonProperty(value = "EVENT_TIMESTAMP", required = true)
	@JacksonXmlProperty(localName = "Extended_Timestamp")
	private OffsetDateTime extendedTimestamp;
	@JsonProperty("GLOBAL_UID")
	@JacksonXmlProperty(localName = "Global_Uid")
	private String globalUid;
	@JsonProperty("USERNAME")
	@JacksonXmlProperty(localName = "DB_User")
	private String dbUser;
	@JsonProperty("CLIENT_ID")
	@JacksonXmlProperty(localName = "Client_Id")
	private String clientId;
	@JsonProperty("EXTERNAL_USERID")
	@JacksonXmlProperty(localName = "Ext_Name")
	private String extName;
	@JsonProperty("OS_USERNAME")
	@JacksonXmlProperty(localName = "OS_User")
	private String osUser;
	@JsonProperty("USERHOST")
	@JacksonXmlProperty(localName = "Userhost")
	private String userhost;
	@JsonProperty("OS_PROCESS")
	@JacksonXmlProperty(localName = "OS_Process")
	private String osProcess;
	@JsonProperty("TERMINAL")
	@JacksonXmlProperty(localName = "Terminal")
	private String terminal;
	@JsonProperty("INSTANCE_NUMBER")
	@JacksonXmlProperty(localName = "Instance_Number")
	private Integer instanceNumber;
	@JsonProperty("OBJECT_SCHEMA")
	@JacksonXmlProperty(localName = "Object_Schema")
	private String objectSchema;
	@JsonProperty("OBJECT_NAME")
	@JacksonXmlProperty(localName = "Object_Name")
	private String objectName;
	@JsonProperty("FGA_POLICY_NAME")
	@JacksonXmlProperty(localName = "Policy_Name")
	private String policyName;
	@JsonProperty("NEW_SCHEMA")
	@JacksonXmlProperty(localName = "New_Owner")
	private String newOwner;
	@JsonProperty("NEW_NAME")
	@JacksonXmlProperty(localName = "New_Name")
	private String newName;
	@JsonProperty("ACTION")
	@JacksonXmlProperty(localName = "Action")
	private BigInteger action;
	@JsonProperty("STMT_TYPE")
	@JacksonXmlProperty(localName = "Stmt_Type")
	private Integer stmtType;
	@JsonProperty("TRANSACTION_ID")
	@JacksonXmlProperty(localName = "TransactionId")
	private String transactionId;
	@JsonProperty("RETURN_CODE")
	@JacksonXmlProperty(localName = "Returncode")
	private BigInteger returncode;
	@JsonProperty("SCN")
	@JacksonXmlProperty(localName = "Scn")
	private BigInteger scn;
	@JsonProperty("COMMENT_TEXT")
	@JacksonXmlProperty(localName = "Comment_Text")
	private String commentText;
	@JsonProperty("AUTH_PRIVILEGES")
	@JacksonXmlProperty(localName = "AuthPrivileges")
	private String authPrivileges;
	@JsonProperty("OS_PRIVILEGE")
	@JacksonXmlProperty(localName = "OSPrivilege")
	private String osPrivilege;
	@JsonProperty("GRANTEE")
	@JacksonXmlProperty(localName = "Grantee")
	private String grantee;
	@JsonProperty("PRIV_USED")
	@JacksonXmlProperty(localName = "Priv_Used")
	private BigInteger privUsed;
	@JsonProperty("SES_ACTIONS")
	@JacksonXmlProperty(localName = "SesActions")
	private String sesActions;
	@JsonProperty("OBJECT_EDITION")
	@JacksonXmlProperty(localName = "Obj_Edition_Name")
	private String objEditionName;
	@JsonProperty("PRIV_GRANTED")
	@JacksonXmlProperty(localName = "Priv_Granted")
	private BigInteger privGranted;
	@JsonProperty("ECONTEXT_ID")
	@JacksonXmlProperty(localName = "EContext_Id")
	private String eContextId;
	@JsonProperty("DBID")
	@JacksonXmlProperty(localName = "DBID")
	private BigInteger dbid;
	@JsonProperty("SQL_BINDS")
	@JacksonXmlProperty(localName = "Sql_Bind")
	private WithBase64Encoding sqlBind;
	@JsonProperty("SQL_TEXT")
	@JacksonXmlProperty(localName = "Sql_Text")
	private WithBase64Encoding sqlText;
	@JsonProperty("RLS_INFO")
	@JacksonXmlProperty(localName = "RLSInformation")
	private WithBase64Encoding rlsInformation;

	private static final Schema SCHEMA_JSON = SchemaBuilder
		.struct()
		.name("oraaud")
		.version(1)
			.field("AUDIT_TYPE", Schema.INT16_SCHEMA)
			.field("SESSIONID", Schema.OPTIONAL_INT64_SCHEMA)
			.field("PROXY_SESSIONID", Schema.OPTIONAL_INT64_SCHEMA)
			.field("STATEMENT_ID", Decimal.builder(0).optional().build())
			.field("ENTRY_ID", Decimal.builder(0).optional().build())
			.field("EVENT_TIMESTAMP", Timestamp.builder().required().build())
			.field("GLOBAL_UID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("USERNAME", Schema.OPTIONAL_STRING_SCHEMA)
			.field("CLIENT_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("EXTERNAL_USERID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("OS_USERNAME", Schema.OPTIONAL_STRING_SCHEMA)
			.field("USERHOST", Schema.OPTIONAL_STRING_SCHEMA)
			.field("OS_PROCESS", Schema.OPTIONAL_STRING_SCHEMA)
			.field("TERMINAL", Schema.OPTIONAL_STRING_SCHEMA)
			.field("INSTANCE_NUMBER", Schema.OPTIONAL_INT32_SCHEMA)
			.field("OBJECT_SCHEMA", Schema.OPTIONAL_STRING_SCHEMA)
			.field("OBJECT_NAME", Schema.OPTIONAL_STRING_SCHEMA)
			.field("FGA_POLICY_NAME", Schema.OPTIONAL_STRING_SCHEMA)
			.field("NEW_SCHEMA", Schema.OPTIONAL_STRING_SCHEMA)
			.field("NEW_NAME", Schema.OPTIONAL_STRING_SCHEMA)
			.field("ACTION", Decimal.builder(0).optional().build())
			.field("STMT_TYPE", Schema.OPTIONAL_INT32_SCHEMA)
			.field("TRANSACTION_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("RETURN_CODE", Decimal.builder(0).optional().build())
			.field("SCN", Decimal.builder(0).optional().build())
			.field("COMMENT_TEXT", Schema.OPTIONAL_STRING_SCHEMA)
			.field("AUTH_PRIVILEGES", Schema.OPTIONAL_STRING_SCHEMA)
			.field("OS_PRIVILEGE", Schema.OPTIONAL_STRING_SCHEMA)
			.field("GRANTEE", Schema.OPTIONAL_STRING_SCHEMA)
			.field("PRIV_USED", Decimal.builder(0).optional().build())
			.field("SES_ACTIONS", Schema.OPTIONAL_STRING_SCHEMA)
			.field("OBJECT_EDITION", Schema.OPTIONAL_STRING_SCHEMA)
			.field("PRIV_GRANTED", Decimal.builder(0).optional().build())
			.field("ECONTEXT_ID", Schema.OPTIONAL_STRING_SCHEMA)
			.field("DBID", Decimal.builder(0).optional().build())
			.field("SQL_BINDS", Schema.OPTIONAL_STRING_SCHEMA)
			.field("SQL_TEXT", Schema.OPTIONAL_STRING_SCHEMA)
			.field("RLS_INFO", Schema.OPTIONAL_STRING_SCHEMA)
		.build();

	@JsonIgnore
	public SourceRecord sourceRecord(final String topic) {
		final Struct struct = new Struct(SCHEMA_JSON);
		struct.put("AUDIT_TYPE", this.auditType);
		struct.put("SESSIONID", this.sessionId);
		struct.put("PROXY_SESSIONID", this.proxySessionId);
		struct.put("STATEMENT_ID", TypeUtils.int2Dec(this.statementId));
		struct.put("ENTRY_ID", TypeUtils.int2Dec(this.entryId));
		struct.put("EVENT_TIMESTAMP", TypeUtils.odt2Timestamp(this.extendedTimestamp));
		struct.put("GLOBAL_UID", this.globalUid);
		struct.put("USERNAME", this.dbUser);
		struct.put("CLIENT_ID", this.clientId);
		struct.put("EXTERNAL_USERID", this.extName);
		struct.put("OS_USERNAME", this.osUser);
		struct.put("USERHOST", this.userhost);
		struct.put("OS_PROCESS", this.osProcess);
		struct.put("TERMINAL", this.terminal);
		struct.put("INSTANCE_NUMBER", this.instanceNumber);
		struct.put("OBJECT_SCHEMA", this.objectSchema);
		struct.put("OBJECT_NAME", this.objectName);
		struct.put("FGA_POLICY_NAME", this.policyName);
		struct.put("NEW_SCHEMA", this.newOwner);
		struct.put("NEW_NAME", this.newName);
		struct.put("ACTION", TypeUtils.int2Dec(this.action));
		struct.put("STMT_TYPE", this.stmtType);
		struct.put("TRANSACTION_ID", this.transactionId);
		struct.put("RETURN_CODE", TypeUtils.int2Dec(this.returncode));
		struct.put("SCN", TypeUtils.int2Dec(this.scn));
		struct.put("COMMENT_TEXT", this.commentText);
		struct.put("AUTH_PRIVILEGES", this.authPrivileges);
		struct.put("OS_PRIVILEGE", this.osPrivilege);
		struct.put("GRANTEE", this.grantee);
		struct.put("PRIV_USED", TypeUtils.int2Dec(this.privUsed));
		struct.put("SES_ACTIONS", this.sesActions);
		struct.put("OBJECT_EDITION", this.objEditionName);
		struct.put("PRIV_GRANTED", TypeUtils.int2Dec(this.privGranted));
		struct.put("ECONTEXT_ID", this.eContextId);
		struct.put("DBID", TypeUtils.int2Dec(this.dbid));
		if (this.sqlBind != null)
			struct.put("SQL_BINDS", this.sqlBind.getValue());
		if (this.sqlText != null)
			struct.put("SQL_TEXT", this.sqlText.getValue());
		if (this.rlsInformation != null)
			struct.put("RLS_INFO", this.rlsInformation.getValue());
		return new SourceRecord(null, null, topic, SCHEMA_JSON, struct);
	}

	public Short getAuditType() {
		return auditType;
	}
	public void setAuditType(Short auditType) {
		this.auditType = auditType;
	}

	public Long getSessionId() {
		return sessionId;
	}
	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}

	public Long getProxySessionId() {
		return proxySessionId;
	}
	public void setProxySessionId(Long proxySessionId) {
		this.proxySessionId = proxySessionId;
	}

	public BigInteger getStatementId() {
		return statementId;
	}
	public void setStatementId(BigInteger statementId) {
		this.statementId = statementId;
	}

	public BigInteger getEntryId() {
		return entryId;
	}
	public void setEntryId(BigInteger entryId) {
		this.entryId = entryId;
	}

	public OffsetDateTime getExtendedTimestamp() {
		return extendedTimestamp;
	}
	public void setExtendedTimestamp(OffsetDateTime extendedTimestamp) {
		this.extendedTimestamp = extendedTimestamp;
	}

	public String getGlobalUid() {
		return globalUid;
	}
	public void setGlobalUid(String globalUid) {
		this.globalUid = globalUid;
	}

	public String getDbUser() {
		return dbUser;
	}
	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public String getClientId() {
		return clientId;
	}
	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getExtName() {
		return extName;
	}
	public void setExtName(String extName) {
		this.extName = extName;
	}

	public String getOsUser() {
		return osUser;
	}
	public void setOsUser(String osUser) {
		this.osUser = osUser;
	}

	public String getUserhost() {
		return userhost;
	}
	public void setUserhost(String userhost) {
		this.userhost = userhost;
	}

	public String getOsProcess() {
		return osProcess;
	}
	public void setOsProcess(String osProcess) {
		this.osProcess = osProcess;
	}

	public String getTerminal() {
		return terminal;
	}
	public void setTerminal(String terminal) {
		this.terminal = terminal;
	}

	public Integer getInstanceNumber() {
		return instanceNumber;
	}
	public void setInstanceNumber(Integer instanceNumber) {
		this.instanceNumber = instanceNumber;
	}

	public String getObjectSchema() {
		return objectSchema;
	}
	public void setObjectSchema(String objectSchema) {
		this.objectSchema = objectSchema;
	}

	public String getObjectName() {
		return objectName;
	}
	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getPolicyName() {
		return policyName;
	}
	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public String getNewOwner() {
		return newOwner;
	}
	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}

	public String getNewName() {
		return newName;
	}
	public void setNewName(String newName) {
		this.newName = newName;
	}

	public BigInteger getAction() {
		return action;
	}
	public void setAction(BigInteger action) {
		this.action = action;
	}

	public Integer getStmtType() {
		return stmtType;
	}
	public void setStmtType(Integer stmtType) {
		this.stmtType = stmtType;
	}

	public String getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public BigInteger getReturncode() {
		return returncode;
	}
	public void setReturncode(BigInteger returncode) {
		this.returncode = returncode;
	}

	public BigInteger getScn() {
		return scn;
	}
	public void setScn(BigInteger scn) {
		this.scn = scn;
	}

	public String getCommentText() {
		return commentText;
	}
	public void setCommentText(String commentText) {
		this.commentText = commentText;
	}

	public String getAuthPrivileges() {
		return authPrivileges;
	}
	public void setAuthPrivileges(String authPrivileges) {
		this.authPrivileges = authPrivileges;
	}

	public String getOsPrivilege() {
		return osPrivilege;
	}
	public void setOsPrivilege(String osPrivilege) {
		this.osPrivilege = osPrivilege;
	}

	public String getGrantee() {
		return grantee;
	}
	public void setGrantee(String grantee) {
		this.grantee = grantee;
	}

	public BigInteger getPrivUsed() {
		return privUsed;
	}
	public void setPrivUsed(BigInteger privUsed) {
		this.privUsed = privUsed;
	}

	public String getSesActions() {
		return sesActions;
	}
	public void setSesActions(String sesActions) {
		this.sesActions = sesActions;
	}

	public String getObjEditionName() {
		return objEditionName;
	}
	public void setObjEditionName(String objEditionName) {
		this.objEditionName = objEditionName;
	}

	public BigInteger getPrivGranted() {
		return privGranted;
	}
	public void setPrivGranted(BigInteger privGranted) {
		this.privGranted = privGranted;
	}

	public String geteContextId() {
		return eContextId;
	}

	public void seteContextId(String eContextId) {
		this.eContextId = eContextId;
	}

	public BigInteger getDbid() {
		return dbid;
	}
	public void setDbid(BigInteger dbid) {
		this.dbid = dbid;
	}

	public WithBase64Encoding getSqlBind() {
		return sqlBind;
	}
	public void setSqlBind(WithBase64Encoding sqlBind) {
		this.sqlBind = sqlBind;
	}

	public WithBase64Encoding getSqlText() {
		return sqlText;
	}
	public void setSqlText(WithBase64Encoding sqlText) {
		this.sqlText = sqlText;
	}

	public WithBase64Encoding getRlsInformation() {
		return rlsInformation;
	}
	public void setRlsInformation(WithBase64Encoding rlsInformation) {
		this.rlsInformation = rlsInformation;
	}


	public static class WithBase64Encoding {

		@JsonProperty(value = "base64Encoded", required = false)
		@JacksonXmlProperty(localName = "BASE64Encoded", isAttribute = true)
		private Boolean base64Encoded;

		@JacksonXmlText
		private String value;

		public Boolean getBase64Encoded() {
			return base64Encoded;
		}
		public void setBase64Encoded(Boolean base64Encoded) {
			this.base64Encoded = base64Encoded;
		}

		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
	}

}
