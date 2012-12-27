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
package com.ebupt.webjoin.insight.intercept.plugin;

import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalyzer;


public abstract class PluginRuntimeDescriptor {
    public static final String DEFAULT_PUBLISHER = "VMware";
    public static final String DEFAULT_HREF      = "http://www.springsource.org/insight";
    
    String version;
    
    public abstract EndPointAnalyzer[] getEndPointAnalyzers();
    public abstract String getPluginName();
    
    
    protected EndPointAnalyzer[] toArray(EndPointAnalyzer... analyzers) {
        return analyzers;
    }
    
    public final String getVersion() {
        return this.version;
    }
    
    public String getPublisher() {
        return DEFAULT_PUBLISHER;
    }
    
    public String getHref() {
        return DEFAULT_HREF;
    }
    
    public final PluginDescriptor toDescriptor() {
        return new PluginDescriptor(getPluginName(), getVersion(), getPublisher(), getHref());
    }

    @Override
    public String toString() {
        return "<insight:plugin name=\"" + getPluginName() + "\""
                + " version=\"" + getVersion() + "\""
                + " publisher=\"" + getPublisher() + "\""
                + " href=\"" + getHref() + "\""
                + " />"
                ;
    }

}
