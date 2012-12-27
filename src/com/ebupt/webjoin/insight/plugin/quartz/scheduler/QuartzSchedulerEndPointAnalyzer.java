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
package com.ebupt.webjoin.insight.plugin.quartz.scheduler;

import com.ebupt.webjoin.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalysis;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointName;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.trace.Frame;



/**
 * 
 */
public class QuartzSchedulerEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer{
    /**
     * The <U>static</U> score value assigned to endpoints - <B>Note:</B>
     * we return a score of {@link EndPointAnalysis#CEILING_LAYER_SCORE} so as
     * to let other endpoints &quot;beat&quot; this one
     */
	public static final int	DEFAULT_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE;

    public QuartzSchedulerEndPointAnalyzer() {
        super(QuartzSchedulerDefinitions.TYPE);
    }

    @Override
	protected int getDefaultScore(int depth) {
    	return DEFAULT_SCORE;
    }

    @Override
	protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation op=frame.getOperation();
        return new EndPointAnalysis(EndPointName.valueOf(op), op.getLabel(), op.getLabel(), getOperationScore(op, depth), op);
    }
}
