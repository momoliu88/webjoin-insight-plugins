/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ebupt.webjoin.insight.plugin.jms;

import java.util.Collection;
import java.util.Collections;

import com.ebupt.webjoin.insight.intercept.metrics.*;
import com.ebupt.webjoin.insight.intercept.trace.*;
import com.ebupt.webjoin.insight.resource.*;
import com.ebupt.webjoin.insight.util.*;



public abstract class AbstractJMSMetricsGenerator extends AbstractExternalResourceMetricsGenerator {
	private static final String JMS_COUNT_SUFFIX = ":type=counter";
	private final String   jmsMetricKey;
	private final JMSPluginOperationType	jmsType;

	protected AbstractJMSMetricsGenerator(JMSPluginOperationType type) {
		super(type.getOperationType());
		jmsType = type;
		jmsMetricKey = type.getOperationType().getName() + JMS_COUNT_SUFFIX;
	}

	public final JMSPluginOperationType getJMSPluginOperationType () {
		return jmsType;
	}

	@Override
    protected Collection<MetricsBag> addExtraEndPointMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> externalFrames) {
		if (ListUtil.size(externalFrames) <= 0) {
		    return Collections.emptyList();
		}

		MetricsBag    mb=MetricsBag.create(endpointResourceKey, trace.getRange());
		addCounterMetricToBag(trace, mb, createMetricKey(), externalFrames.size());
		return Collections.singletonList(mb);
	}

	@Override
	protected void addExtraFrameMetrics(Trace trace, Frame opTypeFrame, MetricsBag mb) {
		addCounterMetricToBag(trace, mb, createMetricKey(), 1);
	}
	
	final String createMetricKey () {
	    return jmsMetricKey;
	}
}
