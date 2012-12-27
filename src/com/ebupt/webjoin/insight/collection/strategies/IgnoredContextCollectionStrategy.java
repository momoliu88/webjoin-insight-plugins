/**
 * Copyright (c) 2009-2010 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.aspectj.lang.JoinPoint.StaticPart;

import com.ebupt.webjoin.insight.Insight;
import com.ebupt.webjoin.insight.InsightAgentClassloadingHelper;
import com.ebupt.webjoin.insight.InsightAgentPluginsHelper;
import com.ebupt.webjoin.insight.InsightContextIgnoredStateChangeListener;
import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.ltw.InsightClassLoader;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;

 

/**
 * Collection strategy to avoid collection for ignored applications
 */
public class IgnoredContextCollectionStrategy
		extends CollectionStrategyBase
		implements CollectionSettingsUpdateListener, InsightContextIgnoredStateChangeListener {
    static final String GROUP_NAME = "application";
    static final String TYPE_NAME  = "ignore";
    static final CollectionSettingName COLLECTION_STRATEGY = new CollectionSettingName(TYPE_NAME, GROUP_NAME);
    static final String KEY_NAME   = COLLECTION_STRATEGY.getKey();
    static final String APP_KEY_PREFIX = KEY_NAME + ".";
    
    private final ConcurrentHashMap<ApplicationName, Boolean> knownApps = new ConcurrentHashMap<ApplicationName, Boolean>();
    private final CollectionSettingsRegistry registry;
    private final Insight insight;
    
    IgnoredContextCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }
    
    IgnoredContextCollectionStrategy(CollectionSettingsRegistry reg) {
        this(reg, resolveConfiguredInsightInstance());
    }
    
    IgnoredContextCollectionStrategy(CollectionSettingsRegistry reg, Insight insightInstance) {
        registry = reg;
        registry.addListener(this);
        registry.register(COLLECTION_STRATEGY, Boolean.TRUE);

        insight = insightInstance;
        insight.addContextStateChangeListener(this);
        
        for(ApplicationName appName : insight.getIgnoredContexts()) {
            register(appName, true);
        }
    }

    public CollectionSettingName getStrategyName() {
        return COLLECTION_STRATEGY;
    }

    public boolean collect(CollectionAspectProperties aspect, StaticPart staticPoint) {
        ApplicationName appName = findApplicationName();
        return _collect(appName);
    }

    public void updatedContextIgnoredState(ApplicationName appName, boolean ignored) {
		register(appName, ignored);
	}

	boolean _collect(ApplicationName appName) {
		if (appName == null) {
			return true;
		}

		Boolean ignored = knownApps.get(appName);
		if (ignored == null) {
			register(appName, false);
            return true;
		} else if (ignored.booleanValue()) {
			return false;	// debug breakpoint
        } else {
        	return true;
        }
    }
    
    private void register(ApplicationName appName, boolean ignored) {
        Boolean	prev=knownApps.put(appName, Boolean.valueOf(ignored));
        if ((prev == null) /* 1st time */ || (prev.booleanValue() != ignored)) {
        	registry.set(getApplicationCollectionSettingName(appName), Boolean.valueOf(ignored));
        }
    }
    
    CollectionSettingName getApplicationCollectionSettingName(ApplicationName appName) {
        String name = appName.getName();
        return new CollectionSettingName(name, KEY_NAME);
    }

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        String key = name.getKey();
        if (!key.startsWith(APP_KEY_PREFIX)) {
            return;
        }

        String appName = key.substring(APP_KEY_PREFIX.length());
        if (appName.length() <= 0) {
            return;
        }

        ApplicationName app = ApplicationName.valueOf(appName);
        boolean         newIgnored = CollectionSettingsRegistry.getBooleanSettingValue(value);
        Boolean         prev = knownApps.put(app, Boolean.valueOf(newIgnored));
        if ((prev == null) /* 1st time */ || (prev.booleanValue() != newIgnored)) {
            insight.setContextIgnored(appName, newIgnored);
        }
    }
    
    @Override
    public int order() { 
        return RUN_FIRST;
    }
    
    /**
     * Find the current application name from the the thread context class loader
     * 
     * @return current application name (or null if such isn't found/set)
     */
    ApplicationName findApplicationName() {
        InsightAgentClassloadingHelper  helper=InsightAgentClassloadingHelper.getInstance();
        ApplicationName appName = helper.retrieveApplicationName(false);
        
        if (appName == null) {
            InsightClassLoader icl = helper.findInsightClassLoader();
            appName = icl != null ? icl.getApplicationName() : null;
        }
        
        return appName;
    }
    
    static Insight resolveConfiguredInsightInstance () {
    	InsightAgentPluginsHelper.registerInsightAgentPluginsHelper(new InsightAgentClassloadingHelper());
        InsightAgentPluginsHelper	helper=InsightAgentPluginsHelper.getRegisteredInsightAgentPluginsHelper();
        System.out.println("helper helper "+helper);
        if (helper != null) {	// prefer the helper (though the result should be the same)
        	return helper.getInsight();
        } else {
        	return Insight.getConfiguredInstance();
        }
    }
}
