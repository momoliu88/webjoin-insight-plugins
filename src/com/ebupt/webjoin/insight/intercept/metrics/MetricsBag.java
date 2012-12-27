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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;
import com.ebupt.webjoin.insight.intercept.util.time.TimeUtil;
import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.util.IDataPoint;

 

public class MetricsBag {

    public static enum PointType { GAUGE, COUNTER }
        
    private List<String> orderedKeys = new ArrayList<String>();
    private Map<String, List<IDataPoint>> points = new HashMap<String, List<IDataPoint>>();
    private Map<String, PointType> types = new HashMap<String, PointType>();
    private TimeRange timeRange;
    private ResourceKey resourceKey;

    private MetricsBag(ResourceKey key, TimeRange range) {
        if (((timeRange=range) == null) || ((resourceKey=key) == null)) {
        	throw new IllegalStateException("<init>(" + key + ")[" + range + "] incomplete specification");
        }
    }

    public static MetricsBag create(ResourceKey resourceKey, TimeRange timeRange) {
        return new MetricsBag(resourceKey, timeRange);
    }

    public MetricsBag add(IDataPoint point, String metricKey) {
        PointType type = getMetricType(metricKey);
        if (type == null) {
            throw new IllegalStateException("The metric key '" + metricKey + "' is not registered with a type. Please call add(metricKey, type) before adding points");
        }

        assertPointFallsWithinRange(point);

        List<IDataPoint> list = points.get(metricKey);
        if (list == null) {
            list = new ArrayList<IDataPoint>();
            points.put(metricKey, list);
        }
        list.add(point);
        return this;
    }

    private void assertPointFallsWithinRange(IDataPoint point) {
        int pointSeconds = point.getTime();
        int rangeStartSeconds = TimeUtil.nanosToSeconds(timeRange.getStart());
        int rangeEndSeconds = TimeUtil.nanosToSeconds(timeRange.getEnd());
        if (pointSeconds < rangeStartSeconds || pointSeconds > rangeEndSeconds) {
            throw new IllegalStateException("The point time '" + point.getTimeStamp() + "' does not fall within the defined time range: [" + timeRange.getStart() + " - " + timeRange.getEnd() + "]");
        }
    }

    public MetricsBag add(String metricKey, PointType type) {
        PointType existing = getMetricType(metricKey);
        if (existing == null) {
            types.put(metricKey, type);
            orderedKeys.add(metricKey);
        }
        else if (existing != type) {
            throw new IllegalStateException("The metric key '" + metricKey + "' is already registered with the type '" + type +"'");
        }
        return this;
    }
    
    public List<IDataPoint> getPoints(String metricKey) {
        List<IDataPoint> list = points.get(metricKey);
        if (list == null) {
            throw new IllegalArgumentException("No points found for key: " + metricKey);
        }
        return Collections.unmodifiableList(list);
    }
    
    public ResourceKey getResourceKey() {
		return resourceKey;
	}
    
    public List<String> getMetricKeys() {
        return Collections.unmodifiableList(orderedKeys);
    }
    
    public PointType getMetricType(String metricKey) {
        return types.get(metricKey);
    }

    public TimeRange getTimeRange() {
        return timeRange;
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

	@Override
	public String toString() {
		return getResourceKey()
			 + ": " + getTimeRange()
			 + " " + points
			 ;
	}
    
}
