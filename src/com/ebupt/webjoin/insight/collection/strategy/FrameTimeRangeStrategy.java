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

package com.ebupt.webjoin.insight.collection.strategy;

import java.io.Serializable;

import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.trace.Frame;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.util.time.TimeRange;

 

public class FrameTimeRangeStrategy extends EnabledPostCollectionStrategy {
    private static final String MIN_OPERATION_DURATION = "min-operation-duration";
    private static final CollectionSettingName CS_NAME = new CollectionSettingName("duration", MIN_OPERATION_DURATION);
    
    private FrameBuilder builder;
    private Long min;
    
    FrameTimeRangeStrategy() {
        this(InterceptConfiguration.getInstance().getFrameBuilder());
    }
    
    FrameTimeRangeStrategy(FrameBuilder frameBuilder) {
        this.builder = frameBuilder;
    }

    public void run(Frame frame) {
        if (min != null) {
            TimeRange range = frame.getRange();
            
            if (range.getDurationMillis() < min.longValue()) {
                builder.discard(frame);
            }
        }
    }

    @Override
    protected void handleIncrementalUpdate(CollectionSettingName name, Serializable value) {
        if (name.equals(CS_NAME)) {
            min = (Long) value;
        }
    }

    @Override
    protected String getRepresentativeName() {
        return MIN_OPERATION_DURATION;
    }

    @Override
    protected boolean isEnabledByDefault() {
        return false;
    }

    @Override
    protected void registerSettings() {
        registry.register(CS_NAME, Long.valueOf(0L));
    }
    
    //for testing
    void setMin(Long value) {
        this.min = value;
    }
}
