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
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.trace.TraceFramesLocator;
import com.ebupt.webjoin.insight.resource.ResourceKey;

/**
 * Examines a {@link Trace} and determines which metrics it contains that may
 * be specific to a plugin. If some metrics are detected to exist, then return
 * them. Otherwise, return <code>null</code>/empty
 */
public interface MetricsGenerator extends TraceFramesLocator {
	/**
	 * @return The {@link OperationType} used by the generator to generate extra metrics
	 */
	OperationType getOperationType ();

    List<MetricsBag> generateMetrics(Trace trace, ResourceKey endpointResourceKey);
    List<MetricsBag> generateMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames);
}
