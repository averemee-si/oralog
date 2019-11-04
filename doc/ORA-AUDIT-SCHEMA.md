

|Field            |Kafka Connect Type                     |UNIFIED\_AUDIT\_TRAIL mapping|DBA\_AUDIT\_TRAIL mapping    |
|:----------------|:--------------------------------------|:------------------------- |:--------------------------|
|AUDIT\_TYPE       |int16                                  |AUDIT_TYPE                 |                           |
|SESSIONID        |int64                                  |SESSIONID                  |SESSIONID                  |
|PROXY\_SESSIONID  |int64                                  |PROXY\_SESSIONID            |PROXY\_SESSIONID            |
|STATEMENT\_ID     |org.apache.kafka.connect.data.Decimal  |STATEMENT\_ID               |                           |
|ENTRY\_ID         |org.apache.kafka.connect.data.Decimal  |ENTRY\_ID                   |ENTRYID                    |
|EVENT\_TIMESTAMP  |org.apache.kafka.connect.data.Timestamp|EVENT\_TIMESTAMP            |EXTENDED\_TIMESTAMP         |
|GLOBAL\_UID       |string                                 |GLOBAL\_USERID              |GLOBAL\_UID                 |
|USERNAME         |string                                 |DBUSERNAME                 |USERNAME                   |
|CLIENT\_ID        |string                                 |CLIENT\_IDENTIFIER          |CLIENT\_ID                  |
|EXTERNAL_USERID  |string                                 |EXTERNAL_USERID            |                           |
|OS\_USERNAME      |string                                 |OS\_USERNAME                |OS\_USERNAME                |
|USERHOST         |string                                 |USERHOST                   |USERHOST                   |
|OS\_PROCESS       |string                                 |OS\_PROCESS                 |OS\_PROCESS                 |
|TERMINAL         |string                                 |TERMINAL                   |TERMINAL                   |
|INSTANCE\_NUMBER  |int32                                  |INSTANCE\_ID                |INSTANCE\_NUMBER            |
|OBJECT\_SCHEMA    |string                                 |OBJECT\_SCHEMA              |OWNER                      |
|OBJECT\_NAME      |string                                 |OBJECT\_NAME                |OBJ\_NAME                   |
|NEW\_SCHEMA       |string                                 |NEW\_SCHEMA                 |NEW\_OWNER                  |
|NEW\_NAME         |string                                 |NEW\_NAME                   |NEW\_NAME                   |
|ACTION           |org.apache.kafka.connect.data.Decimal  |                           |ACTION                     |
|STMT\_TYPE        |int32                                  |                           |                           |
|TRANSACTION\_ID   |string                                 |TRANSACTION\_ID             |TRANSACTIONID              |
|RETURN\_CODE      |string                                 |RETURN\_CODE                |RETURNCODE                 |
|SCN              |org.apache.kafka.connect.data.Decimal  |SCN                        |SCN                        |
|COMMENT\_TEXT     |string                                 |ADDITIONAL\_INFO            |COMMENT\_TEXT               |
|AUTH\_PRIVILEGES  |string                                 |                           |                           |
|OS_PRIVILEGE     |string                                 |                           |                           |
|GRANTEE          |string                                 |                           |GRANTEE                    |
|PRIV\_USED        |org.apache.kafka.connect.data.Decimal  |                           |PRIV\_USED                  |
|SES\_ACTIONS      |string                                 |                           |SES\_ACTIONS                |
|OBJECT\_EDITION   |string                                 |OBJECT\_EDITION             |                           |
|PRIV\_GRANTED     |org.apache.kafka.connect.data.Decimal  |                           |                           |
|DBID             |org.apache.kafka.connect.data.Decimal  |DBID                       |DBID                       |
|SQL\_BINDS        |string                                 |SQL\_BINDS                  |SQL\_BIND                   |
|SQL\_TEXT         |string                                 |SQL\_TEXT                   |SQL\_TEXT                   |
|RLS\_INFO         |string                                 |                           |                           |
|ECONTEXT\_ID      |string                                 |                           |                           |

