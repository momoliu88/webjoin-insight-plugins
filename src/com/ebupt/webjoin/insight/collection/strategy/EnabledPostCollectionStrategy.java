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

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public abstract class EnabledPostCollectionStrategy extends AbstractPostCollectionStrategy {
    private final CollectionSettingName enabledCS;
    private boolean enabled;
    
    EnabledPostCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }
    
    EnabledPostCollectionStrategy(CollectionSettingsRegistry settingsRegistry) {
        super(settingsRegistry);
        
        enabledCS = new CollectionSettingName("enabled", getRepresentativeName());
        String propName = enabledCS.getCollectionSettingsKey();
        String propValue = System.getProperty(propName);
                
        this.enabled = isEnabledByDefault();
        
        if (!StringUtil.isEmpty(propValue)) {
            this.enabled = Boolean.parseBoolean(propValue);
        }
        
        settingsRegistry.register(enabledCS, Boolean.valueOf(this.enabled)); 
    }
    
    public final boolean isEnabled() {
        return enabled;
    }

    public final void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (name.equals(enabledCS)) {
            this.enabled = CollectionSettingsRegistry.getBooleanSettingValue(value);
        } else {
            handleIncrementalUpdate(name, value);
        }
    }
    
    protected abstract void handleIncrementalUpdate(CollectionSettingName name, Serializable value);
    protected abstract String getRepresentativeName();
    protected abstract boolean isEnabledByDefault();

}
