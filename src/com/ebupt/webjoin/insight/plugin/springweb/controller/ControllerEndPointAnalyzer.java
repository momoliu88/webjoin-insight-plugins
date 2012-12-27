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

package com.ebupt.webjoin.insight.plugin.springweb.controller;

import com.ebupt.webjoin.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalysis;
import com.ebupt.webjoin.insight.intercept.endpoint.EndPointName;
import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;

 

/**
 * This trace analyzer simply looks at a Trace and returns a
 * ControllerEndPointAnalysis about what it found.
 * 
 * For a trace to be analyzed, it must be of the following format:
 * 
 * - HttpOperation 
 *    .. 
 *    .. (arbitrary nesting) 
 *      .. ControllerMethodOperation
 */
public class ControllerEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
    public static final OperationType CONTROLLER_METHOD_TYPE = OperationType.valueOf("controller_method");
    /**
     * The property used to mark legacy controller operations
     */
    public static final String	LEGACY_PROPNAME="legacyController";
    /**
     * The <U>static</U> score assigned to legacy controllers - it is just slightly
     * above that of a servlet and/or queue operation
     */
    public static final int	LEGACY_SCORE=EndPointAnalysis.CEILING_LAYER_SCORE + 1;

    public ControllerEndPointAnalyzer () {
    	super(CONTROLLER_METHOD_TYPE);
    }

	@Override
	protected EndPointAnalysis makeEndPoint(Frame controllerFrame, int depth) {
        Operation controllerOp = controllerFrame.getOperation();
        String examplePath = EndPointAnalysis.getHttpExampleRequest(controllerFrame);
        EndPointName endPointName = EndPointName.valueOf(controllerOp);
        String endPointLabel = controllerOp.getLabel();

        return new EndPointAnalysis(endPointName, endPointLabel, examplePath, getOperationScore(controllerOp, depth), controllerOp);
    }

    public String getExampleRequest(Frame httpFrame, Operation controllerOp) {
    	if (httpFrame != null) {
	        Operation operation = httpFrame.getOperation();
	        OperationMap details = operation.get("request", OperationMap.class);
	        return ((details == null) ? "???" : String.valueOf(details.get("method")))
                 + " " + ((details == null) ? "<UNKNOWN>" : details.get(OperationFields.URI));
    	}

    	return controllerOp.getLabel();
    }
}
