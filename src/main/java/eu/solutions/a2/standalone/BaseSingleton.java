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

package eu.solutions.a2.standalone;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import eu.solutions.a2.logs.Constants;
import eu.solutions.a2.logs.oracle.adr.OraAdrPojo;
import eu.solutions.a2.logs.oracle.audit.OraAuditPojo;


public abstract class BaseSingleton implements SendMethodIntf {

	protected int dataFormat = Constants.DATA_FORMAT_RAW_STRING;

	protected static final ObjectWriter writer = new ObjectMapper()
//			.enable(SerializationFeature.INDENT_OUTPUT)
			.setSerializationInclusion(Include.ALWAYS)
			.writer();
	protected static final ObjectReader readerAdr = new XmlMapper()
			.registerModule(new JavaTimeModule())
			.readerFor(OraAdrPojo.class);
	protected static final ObjectReader readerAud = new XmlMapper()
			.registerModule(new JavaTimeModule())
			.readerFor(OraAuditPojo.class);

	@Override
	public void setMessageFormat(final int dataFormat) {
		this.dataFormat = dataFormat;
	}

}
