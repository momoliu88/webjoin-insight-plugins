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

import com.ebupt.webjoin.insight.util.DataPoint;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 
/**
 * 
 */
public class CollectedMetricDataPoint implements Serializable {
	private static final long serialVersionUID = -3033719326690989850L;

	private MetricsBag.PointType	pointType;
	private String metricKey;
	private DataPoint pointValue;

	// package scope constructor and setters for de-serialization
	CollectedMetricDataPoint() {
		super();
	}

	public CollectedMetricDataPoint (String key, MetricsBag.PointType type, DataPoint value) {
		if (StringUtil.isEmpty(key) || (type == null) || (value == null)) {
			throw new IllegalStateException("Incomplete arguments: key=" + key + ";type=" + type + ";value=" + value);
		}

		metricKey = key;
		pointValue = value;
		pointType = type;
	}

	public MetricsBag.PointType getPointType() {
		return pointType;
	}

	public String getMetricKey() {
		return metricKey;
	}

	public DataPoint getPointValue() {
		return pointValue;
	}

	void setPointType(MetricsBag.PointType type) {
		this.pointType = type;
	}

	void setMetricKey(String key) {
		this.metricKey = key;
	}

	void setPointValue(DataPoint value) {
		this.pointValue = value;
	}

	@Override
	public int hashCode() {
		return ObjectUtil.hashCode(getMetricKey())
 			 + ObjectUtil.hashCode(getPointType())
 			 + ObjectUtil.hashCode(getPointValue())
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

        CollectedMetricDataPoint	other=(CollectedMetricDataPoint) obj;
        return ObjectUtil.typedEquals(getPointType(), other.getPointType())
        	&& ObjectUtil.typedEquals(getMetricKey(), other.getMetricKey())
        	&& ObjectUtil.typedEquals(getPointValue(), other.getPointValue())
        	;
	}

	@Override
	public String toString() {
		return getMetricKey() + "[" + getPointType() + "]: " + getPointValue();
	}

}
