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

import java.util.List;

import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.Trace;

 

/**
 * Examines a {@link Trace} and/or {@link Frame} to find an item it considers
 * to be an {@link EndPointAnalysis} 
 */
public interface EndPointAnalyzer {
    /**
     * @param trace The {@link Trace} to be analyzed
     * @return The {@link EndPointAnalysis} of the extracted endpoint or
     * <code>null</code> if the analyzer does not know any suitable endpoint
     */
    EndPointAnalysis locateEndPoint(Trace trace);
    /**
     * @param trace The {@link Trace} to be analyzed
     * @return The {@link Frame} from which the score can be extracted via
     * the call to {@link #getScore(Frame, int)}  or <code>null</code> if the
     * analyzer cannot extract any such frame. <B>Note:</B> this usually
     * matches the analyzer's capability of extract an endpoint via the
     * {@link #locateEndPoint(Trace)} call - i.e., if <code>null</code> is
     * returned from this method, then it is assumed that so would be the
     * case for the other call
     */
    Frame getScoringFrame (Trace trace);
    /**
     * @param frame The {@link Frame} to be analyzed
     * @param depth The frame's depth from the top of the trace stack
     * @return The {@link EndPointAnalysis} of the extracted endpoint or
     * <code>null</code> if the analyzer does not know any suitable endpoint
     */
    EndPointAnalysis locateEndPoint(Frame frame, int depth);
    /**
     * @param frame The {@link Frame} to be analyzed
     * @param depth The frame's depth from the top of the trace stack
     * @return The endpoint score 
     */
    int getScore(Frame frame, int depth);
    /**
     * @return The analyzer's associated operation types - or <code>null</code>
     */
    List<OperationType> getOperationTypes();

}
