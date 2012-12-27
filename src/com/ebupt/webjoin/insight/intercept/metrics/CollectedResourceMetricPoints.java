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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;
import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.ObjectUtil;

 

/**
 * 
 */
public class CollectedResourceMetricPoints implements Serializable {
	private static final long serialVersionUID = 6674914243093045608L;

	private ResourceKey	resourceKey;
	private TimeRange	timeRange;
	private List<CollectedMetricDataPoint>	dataPoints;

	// package scope constructor and setters for de-serialization
	CollectedResourceMetricPoints() {
		super();
	}

	public CollectedResourceMetricPoints (ResourceKey key, TimeRange range, Collection<CollectedMetricDataPoint> points) {
		if (((resourceKey=key) == null) || ((timeRange=range) == null)) {
			throw new IllegalStateException("Incomplete arguments: key=" + key + ";range=" + range);
		}

		dataPoints = (ListUtil.size(points) <= 0)
				? Collections.<CollectedMetricDataPoint>emptyList()
				: new ArrayList<CollectedMetricDataPoint>(points)
				;
	}

	public ResourceKey getResourceKey() {
		return resourceKey;
	}

	public TimeRange getTimeRange() {
		return timeRange;
	}

	public List<CollectedMetricDataPoint> getDataPoints() {
		return dataPoints;
	}


	public void setResourceKey(ResourceKey key) {
		this.resourceKey = key;
	}

	public void setTimeRange(TimeRange range) {
		this.timeRange = range;
	}

	void setDataPoints(List<CollectedMetricDataPoint> points) {
		dataPoints = points;
	}

	@Override
	public int hashCode() {
		return ObjectUtil.hashCode(getResourceKey())
			 + ObjectUtil.hashCode(getTimeRange())
			 + ListUtil.size(getDataPoints())
			 ;
	}

	@Override
	public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        CollectedResourceMetricPoints	other=(CollectedResourceMetricPoints) obj;
        return ObjectUtil.typedEquals(getResourceKey(), other.getResourceKey())
        	&& ObjectUtil.typedEquals(getTimeRange(), other.getTimeRange())
        	&& ListUtil.compareCollections(getDataPoints(), other.getDataPoints())
        	 ;
	}

	@Override
	public String toString() {
		return getResourceKey() + "/" + getTimeRange() + ": " + getDataPoints();
	}

}
