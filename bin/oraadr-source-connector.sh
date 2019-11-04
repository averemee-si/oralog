#!/bin/sh
#
# Copyright (c) 2018-present, http://a2-solutions.eu
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
# compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License is
# distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
# the License for the specific language governing permissions and limitations under the License.
#

if type -p java; then
	JAVA=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
	JAVA="$JAVA_HOME/bin/java"
else
	echo "Unable to find java executable. Exiting!!!"
	exit 1
fi

THISCOMMAND="$0"
while [ -h "$THISCOMMAND" ]; do
	COMMANDDIR="$( cd -P "  $( dirname "$THISCOMMAND" )" >/dev/null 2>&1 && pwd )"
	THISCOMMAND="$(readlink "$THISCOMMAND")"
	[[ $THISCOMMAND != /* ]] && THISCOMMAND="$COMMANDDIR/$THISCOMMAND"
done
COMMANDDIR="$( cd -P "$( dirname "$THISCOMMAND" )" >/dev/null 2>&1 && pwd )"

if [ -z ${A2_LOG_HOME+x} ]; then
	A2_LOG_HOME="$(dirname "$COMMANDDIR")"
elif [ !  -d "$A2_LOG_HOME" ]; then
	echo "A2_LOG_HOME is set to non existent path '$A2_LOG_HOME'. Exiting!!!"
	exit 1
fi

if [[ -n "$KAFKA_HOME" ]] && [[ -x "$KAFKA_HOME/bin/connect-standalone.sh" ]];  then
	CLASSPATH=$A2_LOG_HOME/lib/commons-io-2.6.jar:$A2_LOG_HOME/lib/jackson-core-2.10.0.jar:$A2_LOG_HOME/lib/jackson-dataformat-xml-2.10.0.jar:$A2_LOG_HOME/lib/jackson-datatype-jsr310-2.10.0.jar:$A2_LOG_HOME/lib/stax2-api-4.2.jar
	export CLASSPATH
	$KAFKA_HOME/bin/connect-standalone.sh $KAFKA_HOME/config/connect-standalone.properties $A2_LOG_HOME/conf/oraadr-source-connector.conf
else
	echo "KAFKA_HOME must be set in environment and point to top of Apache Kafka installation. Exiting!!!"
	exit 1
fi
