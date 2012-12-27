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

package com.ebupt.webjoin.insight.application;

import java.util.HashMap;
import java.util.Map;

import com.ebupt.webjoin.insight.util.StringUtil;

 
public class ApplicationMetadataCache {
    private static final ApplicationMetadataCache instance = new ApplicationMetadataCache();   
    //cache: applicationname => applicationMetadata
    private final Map<ApplicationName, ApplicationMetadata> cache=new HashMap<ApplicationName, ApplicationMetadata>();
    
    ApplicationMetadataCache() {
        super();
    }
    
    /*
     * Use a singleton pattern as spring is not available
     */
    public static final ApplicationMetadataCache getInstance() {
        return instance;
    }

    public int size () {
        synchronized (cache) {
            return cache.size();
        }
    }

    public void clear() {
        synchronized (cache) {
            cache.clear();
        }
    }

    public String getLabel(ApplicationName name) {
        synchronized (cache) {
            ApplicationMetadata metadata = cache.get(name);
            if (metadata != null) {
                String label = metadata.getLabel();
                if (!StringUtil.isEmpty(label)) {
                    return label;
                }
            }
        }

        // default to the formatted ApplicationName
        return name.getFormatted();
    }
    
    public void setLabel(ApplicationName name, String label) {
        synchronized (cache) {
            ApplicationMetadata metadata = cache.get(name);
            if (metadata == null) {
                metadata = new ApplicationMetadata(label);
                cache.put(name, metadata);
            } else {
                metadata.setLabel(label);
            }
        }
    }
    
    public ApplicationName getApplicationName(ApplicationName name) {
        synchronized (cache) {
            ApplicationMetadata metadata = cache.get(name);
            if (metadata != null) {
                ApplicationName nameOverride = metadata.getApplicationName();
                if (nameOverride != null) {
                    return nameOverride;
                }
            }
        }

        // default to the provided ApplicationName
        return name;
    }
    
    public void setApplicationName(ApplicationName name, ApplicationName nameOverride) {
        synchronized (cache) {
            ApplicationMetadata metadata = cache.get(name);
            if (metadata == null) {
                metadata = new ApplicationMetadata(nameOverride);
                cache.put(name, metadata);
            } else {
                metadata.setApplicationName(nameOverride);
            }
        }
    }

    private static class ApplicationMetadata {
        private String label;       
        private ApplicationName name;

        public ApplicationMetadata (String lbl) {
            label = lbl;
        }

        public ApplicationMetadata (ApplicationName app) {
            name = app;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String lbl) {
            this.label = lbl;
        }
        
        public ApplicationName getApplicationName() {
            return name;
        }

        public void setApplicationName(ApplicationName app) {
            this.name = app;
        }
    }
}

