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

import java.time.Duration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;

public class CommonJobMgmt implements DynamicMBean {

	private final long startTimeMillis = System.currentTimeMillis();
	private final AtomicLong recordCount = new AtomicLong(0L);
	private final AtomicLong bytesSent = new AtomicLong(0L);
	private final AtomicLong processingTime = new AtomicLong(0L);

	private final static String PARAM_ELAPSED_TIME_MILLIS = "elapsedTimeMillis";
	private final static String PARAM_ELAPSED_TIME = "elapsedTime";
	private static final String PARAM_TRANSFER_TIME_MILLIS = "transferTimeMillis";
	private static final String PARAM_TRANSFER_TIME = "transferTime";
	private static final String PARAM_RECORD_COUNT = "recordCount";
	private static final String PARAM_BYTES_SENT = "bytesSent";

	private final Map<String, Object> parameters = new LinkedHashMap<>();

	CommonJobMgmt() {
		//TODO - different for ADR & AUD
		parameters.put(PARAM_ELAPSED_TIME_MILLIS, startTimeMillis);
		parameters.put(PARAM_ELAPSED_TIME, "");
		parameters.put(PARAM_TRANSFER_TIME_MILLIS, processingTime);
		parameters.put(PARAM_TRANSFER_TIME, "");
		parameters.put(PARAM_RECORD_COUNT, recordCount);
		parameters.put(PARAM_BYTES_SENT, bytesSent);
	}

	@Override
	public synchronized Object getAttribute(String attribute)
			throws AttributeNotFoundException, MBeanException, ReflectionException {
		final Object attrValue = parameters.get(attribute);
		if (attrValue != null)
			switch (attribute) {
			case PARAM_ELAPSED_TIME_MILLIS:
				return (System.currentTimeMillis() - startTimeMillis);
			case PARAM_ELAPSED_TIME:
				return durationAsString(System.currentTimeMillis() - startTimeMillis);
			case PARAM_TRANSFER_TIME:
				return durationAsString(processingTime.longValue());
			default:
				if (attrValue instanceof AtomicLong)
					return ((AtomicLong)attrValue).longValue();
				else
					return attrValue;
			}
		else
			throw new AttributeNotFoundException("No such attribute -> " + attribute);
	}

	@Override
	public synchronized void setAttribute(Attribute attribute)
			throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
		// We're not setting yet any attribute dynamically... yet!
		return;
	}

	@Override
	public synchronized AttributeList getAttributes(String[] attributes) {
		final AttributeList attributeList = new AttributeList();
		for (String attribute : attributes) {
			final Object attrValue = parameters.get(attribute);
			if (attrValue != null) {
				switch (attribute) {
				case PARAM_ELAPSED_TIME_MILLIS:
					attributeList.add(new Attribute(attribute,
							System.currentTimeMillis() - startTimeMillis));
					break;
				case PARAM_ELAPSED_TIME:
					attributeList.add(new Attribute(attribute,
							durationAsString(System.currentTimeMillis() - startTimeMillis)));
					break;
				case PARAM_TRANSFER_TIME:
					attributeList.add(new Attribute(attribute,
							durationAsString(processingTime.longValue())));
					break;
				default:
					if (attrValue instanceof AtomicLong)
						attributeList.add(new Attribute(attribute,
								((AtomicLong)attrValue).longValue()));
					else
						attributeList.add(new Attribute(attribute,
								attrValue));
				}
			}
		}
		return attributeList;
	}

	@Override
	public synchronized AttributeList setAttributes(AttributeList attributes) {
		// We're not setting yet any attribute dynamically... yet!
		return null;
	}

	@Override
	public Object invoke(String actionName, Object[] params, String[] signature)
			throws MBeanException, ReflectionException {
		// We're not invoking any method dynamically... yet!
		return null;
	}

	@Override
	public MBeanInfo getMBeanInfo() {
		MBeanAttributeInfo[] attrs = new MBeanAttributeInfo[parameters.size()];
		Iterator<Entry<String, Object>> it = parameters.entrySet().iterator();
		for (int i = 0; i < attrs.length; i++) {
			Entry<String, Object> attribute = it.next();
			if (attribute.getValue() instanceof AtomicLong ||
					attribute.getValue() instanceof Long) {
				attrs[i] = new MBeanAttributeInfo(attribute.getKey(), "java.lang.Long",
						"Property " + attribute.getKey(), true, false, false);
			} else if (attribute.getValue() instanceof String) {
				attrs[i] = new MBeanAttributeInfo(attribute.getKey(), "java.lang.String",
						"Property " + attribute.getKey(), true, false, false);
			}
		}
		return new MBeanInfo(this.getClass().getName(), "oralog MBean", attrs,
                null, null, null);
	}

	public void addRecordData(final long recordSize, final long elapsedMillis) {
		recordCount.incrementAndGet();
		bytesSent.addAndGet(recordSize);
		processingTime.addAndGet(elapsedMillis);
	}

	public void addRecordData(final long recordNo, final long recordSize, final long elapsedMillis) {
		recordCount.addAndGet(recordNo);
		bytesSent.addAndGet(recordSize);
		processingTime.addAndGet(elapsedMillis);
	}

	public long getRecordCount() {
		return recordCount.longValue();
	}

	private String durationAsString(final long millis) {
		final Duration duration = Duration.ofMillis(millis);
		return String.format("%sdays %shrs %smin %ssec.\n",
				duration.toDays(),
				duration.toHours() % 24,
				duration.toMinutes() % 60,
				duration.getSeconds() % 60);
	}

}
