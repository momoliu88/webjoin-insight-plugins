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

import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.resource.ResourceKey;

 

/**
 * Generates metrics for external resource {@link Frame}-s
 */
public interface ExternalResourceMetricsGenerator extends MetricsGenerator {
    /**
     * @return <code>true</code> if generates extra metrics for the endpoint
     * and not only for the external resource frames. If <code>false</code>
     * then call to {@link #generateEndpointMetrics(Trace, ResourceKey)}
     * will not yield anything
     */
    boolean isGeneratingEndpointMetrics ();
    
    /**
     * Called in order to add extra metrics not related to the external frames
     * @param trace The {@link Trace} instance to be analyzed
     * @param endpointResourceKey The endpoint {@link ResourceKey}
     * @return A {@link Collection} of extra {@link MetricsBag} for the endpoint
     * - or empty if none
     * @see #isGeneratingEndpointMetrics()
     */
    Collection<MetricsBag> generateEndpointMetrics (Trace trace, ResourceKey endpointResourceKey);
}
