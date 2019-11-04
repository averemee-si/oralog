# oralog

Ingestion tool for quick data collection. Purpose of **oralog** is in loading various data based on database audit and diagnostics log to Hadoop, Elasticsearch, Amazon S3 or other tools for further processing and analyzing. **oralog** not just sends data but converts data to JSON format too.
The following data are supported by **oralog**:
- [Oracle Database](https://www.oracle.com/database/index.html) [Automatic Diagnostic Repository](https://docs.oracle.com/en/database/oracle/oracle-database/12.2/admin/managing-diagnostic-data.html#GUID-8DEB1BE0-8FB9-4FB2-A19A-17CF6F5791C3)
- [Oracle Database](https://www.oracle.com/database/index.html) audit files in XML format including extended SQL\_TEXT and SQL\_BIND information (XSD schemas available at: [11.2](http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd), [12.2](http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-12_2.xsd))
- more TODO

**oralog** can operate in different modes as
1. [Kafka Connect](https://docs.confluent.io/current/connect/index.html) source connector
2. [Apache Kafka](http://kafka.apache.org/) producer
3. [Amazon Kinesis](https://aws.amazon.com/kinesis/) producer


## Getting Started

These instructions will get you a copy of the project up and running on your server.

### Prerequisites

Before using **oralog** please check that required Java8+ is installed with

```
echo "Checking Java version"
java -version
```


### Installing

Build with

```
mvn install
```
Then run as root supplied `install.sh` passing target installation directory as script parameter. This directory will be mentioned hereinafter as `A2_LOG_HOME`.  For example:

```
install.sh /opt/a2/oralog
```
This will create following directory structure under `A2_LOG_HOME`

`bin` - shell scripts 
`conf` - configuration files
`doc` - doc files
`lib` - jar files
`log` - producer's log


## Running and using oralog 

### Processing Oracle Automatic Diagnostic Repository files
Please look to [ORALOG4ORAADR.md](doc/ORALOG4ORAADR.md).
For schema used for conversion of Oracle ADR xml to JSON please refer to [adr.xsd](doc/adr.xsd)

### Processing Oracle Audit XML files
Please look to [ORALOG4ORAAUD.md](doc/ORALOG4ORAAUD.md)
Mapping between JSON produced by **oralog** and Oracle RDBMS views available at [ORA-AUDIT-SCHEMA.md](doc/ORA-AUDIT-SCHEMA.md)
For Oracle Database Audit XML schema please visit [http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd](http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-11_2.xsd) and [http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-12_2.xsd](http://xmlns.oracle.com/oracleas/schema/dbserver_audittrail-12_2.xsd)


## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## TODO
* [PostgreSQL](https://www.postgresql.org/) [PGAudit](https://www.pgaudit.org/) support
* Linux [auditd](http://man7.org/linux/man-pages/man8/auditd.8.html) support 

## Version history
####0.7.0####
Initial release


## Authors

* **Aleksej Veremeev** - *Initial work* - [A2 Solutions](http://a2-solutions.eu/)

## License

This project is licensed under the Apache License - see the [LICENSE](LICENSE) file for details

