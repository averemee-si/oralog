### Oracle Database settings and sizing considerations

Please check that audit-related Oracle RDBMS parameters are set correctly

```
sqlplus / as sysdba
select NAME, VALUE from v$parameter where NAME in ('audit_sys_operations', 'audit_file_dest','audit_trail');
REM or use 'show parameter' command
```
Just to google for SYS.AUD$ - around 4000000 results, and most of them performance related, so we recommend to set **audit_file_dest** to separate filesystem/LUN/etc from RDBMS datafiles, tempfiles and logfiles.

We recommend to set **audit_sys_operations** to  **TRUE**.

For working with **oralog** Oracle RDBMS parameter **audit_trail** must be set to **XML, EXTENDED**

Do not forget to align [Kafka](http://kafka.apache.org/)'s message.max.bytes and replica.fetch.max.bytes parameters with Oracle Database audit file size. To check current values in Oracle database use

```
column PARAMETER_NAME format A30
column PARAMETER_VALUE format A20
column AUDIT_TRAIL format A20
select PARAMETER_NAME, PARAMETER_VALUE, AUDIT_TRAIL
from DBA_AUDIT_MGMT_CONFIG_PARAMS
where PARAMETER_NAME like '%SIZE%'
```
To change size of audit xml file

```
BEGIN
  DBMS_AUDIT_MGMT.set_audit_trail_property(
    audit_trail_type           => DBMS_AUDIT_MGMT.AUDIT_TRAIL_XML,
    audit_trail_property       => DBMS_AUDIT_MGMT.OS_FILE_MAX_SIZE,
    audit_trail_property_value => 1000);
END;
/

BEGIN
  DBMS_AUDIT_MGMT.set_audit_trail_property(
    audit_trail_type           => DBMS_AUDIT_MGMT.AUDIT_TRAIL_UNIFIED,
    audit_trail_property       => DBMS_AUDIT_MGMT.OS_FILE_MAX_SIZE,
    audit_trail_property_value => 1000);
END;
/

BEGIN
  DBMS_AUDIT_MGMT.set_audit_trail_property(
    audit_trail_type           => DBMS_AUDIT_MGMT.AUDIT_TRAIL_OS,
    audit_trail_property       => DBMS_AUDIT_MGMT.OS_FILE_MAX_SIZE,
    audit_trail_property_value => 1000);
END;
/
```
There is comparision of [Apache Kafka](http://kafka.apache.org/) performance and throughtput for different message size - [Finding Kafka optimal message size](https://www.idata.co.il/2018/02/finding-kafka-optimal-message-size/) and we recommend not to set Oracle Database audit file size higher than 1M.
[Amazon Kinesis](https://aws.amazon.com/kinesis/) does not support message size greater than 1M.

Create Oracle Database FGA rule

```
begin
 DBMS_FGA.ADD_POLICY(
	object_schema => 'AP',
	object_name => 'AP_INVOICES_ALL',
	policy_name => 'AP_INV_AUD',
    enable => TRUE,
	statement_types => 'INSERT,UPDATE,SELECT',
	audit_trail => DBMS_FGA.XML + DBMS_FGA.EXTENDED);
end;
/
```
or use Oracle Database audit command 

```
AUDIT SELECT
  ON per.per_all_people_f;
```

For other examples please consult Oracle Database documentation.


### Configuration for Kafka Connect
Review and edit template configuration file `$A2_LOG_HOME/conf/oraaudit-source-connector.conf`.
Parameters for running are:
`a2.watched.path` - path to Oracle RDBMS `audit_file_dest`. Mandatory parameter
`a2.target.topic` - target Apache Kafka topic.
`a2.data.format` - format to send data: RAW or JSON when data converted to JSON
`a2.locked.file.query.interval` - interval in milliseconds between checks for end of writes to this file
`a2.poll.interval` - frequency in milliseconds to poll for new data for each task

To run audit file processing in Kafka Connect mode please review and edit if needed supplied `$A2_LOG_HOME/bin/oraaudit-source-connector.sh`. For running this script `$KAFKA_HOME` environment variable must be set and points to valid home of Apache Kafka installation

```
export KAFKA_HOME=/opt/kafka_2.12-2.2.1
$A2_LOG_HOME/bin/oraaudit-source-connector.sh

```

### Configuration for Apache Kafka

Create topic using command line interface, for example: 

```
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic oraaud-test
```

Don't forget about correct sizing of topic for heavy load.
If you using Amazon Managed Streaming for Apache Kafka you can use [AWS Management Console](https://console.aws.amazon.com/msk) 

Edit `oraaud-kafka.conf`, this files should looks like

```
a2.watched.path = /data/oracle/adump
a2.worker.count = 32
a2.locked.file.query.interval = 512
a2.kafka.servers = dwh.a2-solutions.eu:9092
a2.kafka.topic = ora-audit-topic
a2.kafka.client.id = a2.audit.ai.ora112

```
#### Mandatory parameters ####
`a2.watched.path` - valid directory path, must match Oracle RDBMS parameter **audit_file_dest**

`a2.worker.count` - number of threads for transferring audit information to Kafka cluster

`a2.locked.file.query.interval` - interval in milliseconds between check for "file in use"

`a2.kafka.servers` - hostname/IP address and port of Kafka installation

`a2.kafka.topic` - value must match name of Kafka topic created on previous step

`a2.kafka.client.id` - use any valid string value for identifying this Kafka producer


#### Optional parameters ####
`a2.kafka.security.protocol` - must be set to `SSL` or `SASL_SSL` if you like to transmit files using SSL and enable auth. Only PLAIN authentication supported and tested at moment.

`a2.kafka.security.truststore.location` - set to valid certificate store file if `a2.security.protocol` set to `SSL` or `SASL_SSL`

`a2.kafka.security.truststore.password` - password for certificate store file if `a2.security.protocol` set to `SSL` or `SASL_SSL`

`a2.kafka.security.jaas.config` - JAAS login module configuration. Must be set when `a2.security.protocol` set to `SASL_SSL`. For example **org.apache.kafka.common.security.plain.PlainLoginModule required username="alice" password="alice-secret";** . Do not forget to escape equal sign and double quotes in file.

`a2.kafka.acks` - number of acknowledgments. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `acks` parameter
 
`a2.kafka.batch.size` - producer batch size. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `batch.size` parameter 

`a2.kafka.buffer.memory` - producer buffer memory. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `buffer.memory` parameter 

`a2.kafka.compression.type` - compression type. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `compression.type` parameter. By default set to `gzip`, to disable compression set to `uncompressed` 

`a2.kafka.linger.ms` - producer linger time. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `linger.ms` parameter

`a2.kafka.max.request.size` - maximum size of producer producer request. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `max.request.size` parameter

`a2.kafka.retries` - producer retries config. Please check [Apache Kafka documentation](https://kafka.apache.org/documentation/#configuration) for more information about `retries` parameter



### Configuration for Amazon Kinesis 
Create Kinesis stream using [AWS Management Console](https://console.aws.amazon.com/kinesis) or using AWS CLI, for example:

```
aws kinesis create-stream --stream-name ora-aud-test --shard-count 1
```
Check stream's creation progress using [AWS Management Console](https://console.aws.amazon.com/kinesis) or with AWS CLI, for example:

```
aws kinesis describe-stream --stream-name ora-aud-test
```
Don't forget about correct sizing of stream for heavy load.

Edit `oraaud-kafka.conf`, this files should looks like

```
a2.target.broker = kinesis
a2.watched.path = /data/oracle/adump
a2.worker.count = 32
a2.locked.file.query.interval = 512
a2.kinesis.region = eu-west-1
a2.kinesis.stream = ora-aud-test
a2.kinesis.access.key = AAAAAAAAAABBBBBBBBBB
a2.kinesis.access.secret = AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD
```
#### Mandatory parameters ####

`a2.target.broker` - must set to **kinesis** for working with Amazon Kinesis 

`a2.watched.path` - valid directory path, must match Oracle RDBMS parameter **audit_file_dest**

`a2.worker.count` - number of threads for transferring audit information to Amazon Kinesis

`a2.locked.file.query.interval` - interval in milliseconds between check for "file in use"

`a2.kinesis.region` - AWS region

`a2.kinesis.stream` - name of Kinesis stream

`a2.kinesis.access.key` - AWS access key

`a2.kinesis.access.secret` - AWS access key secret

#### Optional parameters ####

`a2.kinesis.max.connections` - can be used to control the degree of parallelism when making HTTP requests. Using a high number will cause a bunch of broken pipe errors to show up in the logs. This is due to idle connections being closed by the server. Setting this value too large may also cause request timeouts if you do not have enough bandwidth. **1** is default value

`a2.kinesis.request.timeout` - Request timeout milliseconds. **30000** is default value

`a2.kinesis.request.record.max.buffered.time` - controls how long records are allowed to wait  in the Kinesis Producer's buffers before being sent. Larger values increase aggregation and reduces the number of Kinesis records put, which can be helpful if you're getting throttled because of the records per second limit on a shard.. **5000** is default value

`a2.kinesis.file.size.threshold` - Maximum size of audit file transferred without compression. **512** is default value

