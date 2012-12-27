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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.metrics.MetricsBag.PointType;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.operation.OperationUtils;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.util.time.TimeUtil;
import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.util.DataPoint;
import com.ebupt.webjoin.insight.util.ListUtil;



/**
 * 
 */
public abstract class AbstractMetricsGenerator implements MetricsGenerator {
	public static final String EXECUTION_TIME = "externalExecutionTime:type=response_time";
	public static final String INVOCATION_COUNT = "invocationCount:type=counter";

	private final OperationType opType;

	protected AbstractMetricsGenerator(OperationType type) {
		if ((opType=type) == null) {
			throw new IllegalStateException("No operation type specified");
		}
	}

	public final OperationType getOperationType () {
	    return opType;
	}

	public List<MetricsBag> generateMetrics(Trace trace, ResourceKey endpointResourceKey) {
		return generateMetrics(trace, endpointResourceKey, locateFrames(trace));
	}

	public Collection<Frame> locateFrames(Trace trace) {
		return locateDefaultMetricsFrames(trace, getOperationType());
	}

	public List<MetricsBag> generateMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
		List<MetricsBag> mbs = addMetricsToBag(trace, endpointResourceKey, null, generateFramesMetrics(trace, endpointResourceKey, frames));
		mbs = addMetricsToBag(trace, endpointResourceKey, mbs, addExtraEndPointMetrics(trace, endpointResourceKey, frames));
		if (mbs == null) {
		    return Collections.emptyList();
		}

		return mbs;        
	}

	protected Collection<MetricsBag> generateFramesMetrics (Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
		if (ListUtil.size(frames) <= 0) {
			return Collections.emptyList();
		}

		List<MetricsBag> mbs = new ArrayList<MetricsBag>(frames.size());
	    for (Frame opTypeFrame : frames) {
	    	MetricsBag	mb=createFrameMetricsBag(trace, opTypeFrame, endpointResourceKey);
	    	if (mb == null) {
				continue;
			}

			mbs.add(mb);
		}

	    return mbs;
	}

	protected List<MetricsBag> addMetricsToBag (
			Trace trace, ResourceKey endpointResourceKey, List<MetricsBag> bag, Collection<? extends MetricsBag> metrics) {
		if (ListUtil.size(metrics) <= 0) {
			return bag;
		}
		
		if (bag == null) {
			return new ArrayList<MetricsBag>(metrics);
		}

		bag.addAll(metrics);
		return bag;
	}
	// if returns null then value ignored
	protected MetricsBag createFrameMetricsBag (Trace trace, Frame opTypeFrame, ResourceKey defaultResourceKey) {
		ResourceKey resourceKey=getResourceKey(opTypeFrame, defaultResourceKey);
		if (resourceKey == null) {
			return null;
		}

		MetricsBag mb = MetricsBag.create(resourceKey, trace.getRange());
		addExecutionTimeMetricToBag(opTypeFrame, mb);
		addCounterMetricToBag(trace, mb, INVOCATION_COUNT, 1);

		addExtraFrameMetrics(trace, opTypeFrame, mb);
		return mb;
	}

	/**
	 * Plugins can override this to add metrics to the endpoint's {@link List}
	 * of {@link MetricsBag}'s
	 * @param trace The relevant {@link Trace}
	 * @param endpointResourceKey The default {@link ResourceKey} that can be used to
	 * generate the metrics
	 * @param frames The {@link Collection} of resource {@link Frame}-s
	 * obtained from call to {@link #locateFrames(Trace)}
	 * Used to save plugins from reanalyzing a trace (see rabbit or jms for examples)
	 */
	protected Collection<MetricsBag> addExtraEndPointMetrics(Trace trace, ResourceKey endpointResourceKey, Collection<Frame> frames) {
	    return Collections.emptyList();
	}

	/** 
	 * Plugins can override this to add non default metrics to the resource's {@link MetricsBag}
	 * @param trace The {@link Trace} from which the {@link Frame} originated
	 * @param opTypeFrame  The {@link Frame} from which to create extra metrics
	 * @param mb The {@link MetricsBag} to add the metrics to
	 */
	protected void addExtraFrameMetrics(Trace trace, Frame opTypeFrame, MetricsBag mb) {
	    // nothing extra
	}

	protected ResourceKey getResourceKey(Frame frame, ResourceKey defaultResourceKey) {
	    return getResourceKey(frame.getOperation(), defaultResourceKey);
	}

	/*
	 * NOTE !!! the implementation retrieves only the child key if there is a parent+child
	 * resource key encoded on the same operation
	 * @see METRICS-2945 and METRICS-2935
	 */
	protected ResourceKey getResourceKey(Operation op, ResourceKey defaultResourceKey) {
		return OperationUtils.getResourceKey(op, defaultResourceKey);
	}

	protected DataPoint addExecutionTimeMetricToBag(Frame frame, MetricsBag mb) {
		DataPoint execTime = DataPoint.dataPointFromRange(frame.getRange());
		mb.add(EXECUTION_TIME, PointType.GAUGE);            
		mb.add(execTime, EXECUTION_TIME);
		return execTime;
	}

	protected DataPoint addCounterMetricToBag(Trace trace, MetricsBag mb, String counterMetricKey, int counterValue) {
		mb.add(counterMetricKey, PointType.COUNTER);  
		int time = TimeUtil.nanosToSeconds(trace.getRange().getStart());
		DataPoint count = new DataPoint(time, counterValue);
		mb.add(count, counterMetricKey);
		return count;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + getOperationType() + "]";
	}

	public static final Collection<Frame> locateDefaultMetricsFrames (Trace trace, OperationType type) {
		return trace.getLastFramesOfType(type);
	}
}
