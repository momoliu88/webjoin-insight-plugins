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

package com.ebupt.webjoin.insight.plugin.rabbitmqClient;

import java.util.Collection;
import java.util.Collections;

import com.ebupt.webjoin.insight.intercept.metrics.AbstractExternalResourceMetricsGenerator;
import com.ebupt.webjoin.insight.intercept.metrics.MetricsBag;
import com.ebupt.webjoin.insight.intercept.trace.*;
import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.util.ListUtil;
 

public abstract class AbstractRabbitMetricsGenerator extends AbstractExternalResourceMetricsGenerator {
	public static final String RABBIT_COUNT_SUFFIX = ":type=counter";

	private final String   rabbitMetricKey;

	protected AbstractRabbitMetricsGenerator(RabbitPluginOperationType rabbitOpType) {
		super(rabbitOpType.getOperationType());
		rabbitMetricKey = rabbitOpType.getOperationType().getName() + RABBIT_COUNT_SUFFIX;
	}

	@Override
    protected Collection<MetricsBag> addExtraEndPointMetrics(Trace trace, ResourceKey resourceKey, Collection<Frame> externalFrames) {
        if (ListUtil.size(externalFrames) <= 0) {
            return Collections.emptyList();
        }

        MetricsBag    mb=MetricsBag.create(resourceKey, trace.getRange());
        addCounterMetricToBag(trace, mb, createMetricKey(), externalFrames.size());
        return Collections.singletonList(mb);
	}

	@Override
	protected void addExtraFrameMetrics(Trace trace,  Frame opTypeFrame, MetricsBag mb) {
		addCounterMetricToBag(trace, mb, createMetricKey(), 1);
	}

	final String createMetricKey() {
		return rabbitMetricKey;
	}
}
