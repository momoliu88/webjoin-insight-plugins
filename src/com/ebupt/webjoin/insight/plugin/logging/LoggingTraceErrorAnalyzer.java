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
package com.ebupt.webjoin.insight.plugin.logging;

import java.util.Collections;
import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.Operation;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;
import com.ebupt.webjoin.insight.intercept.trace.TraceError;
import com.ebupt.webjoin.insight.intercept.trace.TraceErrorAnalyzer;


/**
 * 
 */
public class LoggingTraceErrorAnalyzer implements TraceErrorAnalyzer {
    public LoggingTraceErrorAnalyzer() {
        super();
    }

    public List<TraceError> locateErrors(Trace trace) {
        Frame   logFrame=trace.getFirstFrameOfType(LoggingDefinitions.TYPE);
        if (logFrame == null) {
            return Collections.emptyList();
        }

        Operation op = logFrame.getOperation();
        return Collections.singletonList(new TraceError(op.getLabel()));
    }

}
