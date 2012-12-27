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

package com.ebupt.webjoin.insight.intercept.endpoint;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationList;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
 

/**
 * If a MethodOperation is a root-level operation within a Trace, create
 * an EndPoint for it.
 * 
 * A MethodOperation can appear at the root level whenever a method is
 * invoked asynchronously from a web request (such as a background thread,
 * scheduled threads, etc.)
 */
public class TopLevelMethodEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
	private static final TopLevelMethodEndPointAnalyzer	instance=new TopLevelMethodEndPointAnalyzer();

	public static final TopLevelMethodEndPointAnalyzer getInstance () {
		return instance;
	}

    private TopLevelMethodEndPointAnalyzer () {
    	super(OperationType.METHOD);
    }

	@Override
    protected EndPointAnalysis makeEndPoint(Frame frame, int depth) {
        Operation op = frame.getOperation();
        EndPointName name = EndPointName.valueOf(op);
        String label = op.getLabel();
        String exampleRequest = label;
        OperationList args = op.get(OperationFields.ARGUMENTS, OperationList.class);
        if ((args != null) && (args.size() == 1)) {
        	exampleRequest = args.get(0, String.class); 
        }

        return new EndPointAnalysis(name, label, exampleRequest, getOperationScore(op, depth), op);
    }
}
