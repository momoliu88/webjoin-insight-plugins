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
package com.ebupt.webjoin.insight.plugin.jdbc;


import java.util.HashSet;
import java.util.Set;

import com.ebupt.webjoin.insight.intercept.operation.OperationType;
import com.ebupt.webjoin.insight.intercept.operation.filter.OperationFilter;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameId;
import com.ebupt.webjoin.insight.intercept.trace.FrameUtil;
import com.ebupt.webjoin.insight.intercept.trace.Trace;




public class TopLevelJdbcOperationFilter implements OperationFilter {
    private static final OperationType TYPE = JdbcOperationExternalResourceAnalyzer.TYPE;

    public String getFilterLabel() {
        return "JDBC, Top-level";
    }

    public int matchingFrames(Trace trace) {
        return includeFrames(trace).size();
    }

    public Set<FrameId> includeFrames(Trace trace) {
        Set<Frame> frames = FrameUtil.topLevelFramesOfType(trace.getRootFrame(), TYPE);
        return FrameUtil.frameIDs(frames);
    }
    
    public Set<FrameId> excludeFrames(Trace trace) {
        Set<Frame> frames = new HashSet<Frame>();
        for (Frame topLevelFrame : FrameUtil.topLevelFramesOfType(trace.getRootFrame(), TYPE)) {
            frames.addAll(FrameUtil.descendantFramesOfType(topLevelFrame, TYPE));
        }
        return FrameUtil.frameIDs(frames);
    }

    public boolean isInversable() {
        return false;
    }
    
}
