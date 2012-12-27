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

package com.ebupt.webjoin.insight.intercept.metrics;

import java.util.Collection;
import java.util.Collections;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.resource.ResourceNames;

 

public abstract class AbstractExternalResourceMetricsGenerator extends AbstractMetricsGenerator implements ExternalResourceMetricsGenerator {
	private final boolean	endpointMetrics;

	protected AbstractExternalResourceMetricsGenerator(OperationType type) {
		this(type, false);
	}

	protected AbstractExternalResourceMetricsGenerator(OperationType type, boolean generatesEndpointMetrics) {
		super(type);
		endpointMetrics = generatesEndpointMetrics;
	}

	public Collection<MetricsBag> generateEndpointMetrics(Trace trace, ResourceKey endpointResourceKey) {
		if (isGeneratingEndpointMetrics()) {
			return addExtraEndPointMetrics(trace, endpointResourceKey, Collections.<Frame>emptyList());
		} else {
			return Collections.emptyList();
		}
	}

	public final boolean isGeneratingEndpointMetrics() {
		return endpointMetrics;
	}

	@Override
	protected ResourceKey getResourceKey(Operation op, ResourceKey defaultResourceKey) {
		ResourceKey	key=super.getResourceKey(op, defaultResourceKey);
		if (key == null) {
			return null;
		}

		String	typeName=key.getType();
		// make sure this is an external resource key
		if (!ResourceNames.ApplicationServerExternalResource.equals(typeName)) {
			return null;	// TODO log a warning (?)
		}

		return key;
	}
}
