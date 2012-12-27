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
package com.ebupt.webjoin.insight.plugin.mail;

import com.ebupt.webjoin.insight.intercept.metrics.AbstractExternalResourceMetricsGenerator;
import com.ebupt.webjoin.insight.intercept.metrics.MetricsBag;
import com.ebupt.webjoin.insight.intercept.metrics.MetricsBag.PointType;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.util.time.TimeUtil;
import com.ebupt.webjoin.insight.util.DataPoint;



public class MailMetricsGenerator extends AbstractExternalResourceMetricsGenerator {
	public static final String  MAIL_SIZE_METRIC = "mailSize:type=bytes";
	
	public MailMetricsGenerator() {
		super(MailDefinitions.SEND_OPERATION);
	}

	@Override
	protected void addExtraFrameMetrics(Trace trace, Frame opTypeFrame, MetricsBag mb) {
		// Add the message size data point
		Operation	op=opTypeFrame.getOperation();
		Number 		contentSize=op.get("size", Number.class);
		// OK if missing - the size is collected only if extra information is enabled
		if (contentSize == null) {
		    return;
		}

        mb.add(MAIL_SIZE_METRIC, PointType.GAUGE);
		int time = TimeUtil.nanosToSeconds(trace.getRange().getStart());
		DataPoint responseSizePoint = new DataPoint(time, contentSize.doubleValue());
		mb.add(responseSizePoint, MAIL_SIZE_METRIC);		
	}
}
