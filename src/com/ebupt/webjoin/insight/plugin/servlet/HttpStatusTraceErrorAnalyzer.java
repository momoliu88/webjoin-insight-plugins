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
package com.ebupt.webjoin.insight.plugin.servlet;

import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.operation.OperationFields;
import com.ebupt.webjoin.insight.intercept.operation.OperationMap;
import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.trace.TraceError;
import com.ebupt.webjoin.insight.intercept.trace.TraceErrorAnalyzer;



public class HttpStatusTraceErrorAnalyzer implements TraceErrorAnalyzer {

    private static final List<TraceError> contextNotAvailable
            = Collections.singletonList(new TraceError("Context not available"));

    public List<TraceError> locateErrors(Trace trace) {
        Frame httpFrame = trace.getFirstFrameOfType(OperationType.HTTP);
        if (httpFrame == null) {
            return Collections.emptyList();
        }
        Operation op = httpFrame.getOperation();
        OperationMap response = op.get("response", OperationMap.class);

        Integer statusCode = (response == null) ? null : response.get("statusCode", Integer.class);
        if ((statusCode != null) && httpStatusIsError(statusCode.intValue())) {
            return Collections.singletonList(new TraceError("Status code = " + statusCode)); 
        }

        OperationMap request = op.get("request", OperationMap.class);
        Boolean contextAvailable = (request == null) ? null : request.get(OperationFields.CONTEXT_AVAILABLE, Boolean.class);
        if ((contextAvailable != null) && (!contextAvailable.booleanValue())) {
            return contextNotAvailable;
        }
        return Collections.emptyList();
    }

    boolean httpStatusIsError(int status) {
        return status >= 500 && status < 600;
    }

}
