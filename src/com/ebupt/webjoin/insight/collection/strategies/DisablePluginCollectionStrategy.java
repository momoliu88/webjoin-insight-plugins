/**
 * Copyright (c) 2009-2010 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.lang.JoinPoint.StaticPart;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
 
/**
 * Collection strategy to enable/disable plugins
 */
public class DisablePluginCollectionStrategy  extends CollectionStrategyBase implements CollectionSettingsUpdateListener {
    static final String GROUP_NAME = "plugins";
    static final String TYPE_NAME  = "enable";
    static final CollectionSettingName COLLECTION_STRATEGY = new CollectionSettingName(TYPE_NAME, GROUP_NAME);
    static final Pattern PLUGIN_PATTERN = Pattern.compile(GROUP_NAME + "\\.([^.]+)\\." + TYPE_NAME);
    
    private final ConcurrentHashMap<String, Boolean> knownPluginFiles = new ConcurrentHashMap<String, Boolean>();
    private final CollectionSettingsRegistry registry;
    
    DisablePluginCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }
    
    DisablePluginCollectionStrategy(CollectionSettingsRegistry reg) {
        this.registry = reg;
        
        this.registry.addListener(this);
        this.registry.register(COLLECTION_STRATEGY, Boolean.TRUE);
    }

    public CollectionSettingName getStrategyName() {
        return COLLECTION_STRATEGY;
    }

    public boolean collect(CollectionAspectProperties aspect, StaticPart staticPoint) {
        String pluginName = aspect.getPluginName();
        
        if (pluginName == null) {
            return true;
        }
        
        Boolean enabled = knownPluginFiles.get(pluginName);
        
        if (enabled == null) {
            return registerAspect(pluginName);
        } else {
            return enabled.booleanValue();
        }
    }
    
    private synchronized boolean registerAspect(String pluginName) {
        Boolean enabled = knownPluginFiles.get(pluginName);
        
        if (enabled == null) {
            CollectionSettingName   settingName=getPluginCollectionSettingName(pluginName);
            enabled = registry.getBooleanSetting(settingName);
            
            if (enabled == null) {
                knownPluginFiles.put(pluginName, Boolean.TRUE);
                registry.register(settingName, Boolean.TRUE);
            } else {
                knownPluginFiles.put(pluginName, enabled);
            }
            return true;
        } else {
            return enabled.booleanValue();
        }
    }
    
    public static CollectionSettingName getPluginCollectionSettingName(String pluginName) {
        return new CollectionSettingName(pluginName + "." + TYPE_NAME, GROUP_NAME);
    }
    
    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        String key = name.getKey();
        Matcher m = PLUGIN_PATTERN.matcher(key);
        
        if (!m.matches()) {
            return;
        }

        String pluginName = m.group(1);
        boolean disabled = CollectionSettingsRegistry.getBooleanSettingValue(value);
        knownPluginFiles.put(pluginName, Boolean.valueOf(disabled));
    }
    
    @Override
    public int order() { 
        return RUN_FIRST;
    }
}
