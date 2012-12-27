/**
 * Copyright (c) 2009-2010 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;

 
import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;

import java.io.Serializable;

/**
 * CollectionStrategy which allows for disabling all collection on an agent
 */
public class DisableAgentCollectionStrategy extends CollectionStrategyBase implements CollectionSettingsUpdateListener {
    public static final CollectionSettingName name = new CollectionSettingName("enabled", "agent");
    private volatile boolean enabled = true;

    public DisableAgentCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }
    public DisableAgentCollectionStrategy(CollectionSettingsRegistry registry) {
        registry.register(name, Boolean.TRUE); // collection turned on by default
        registry.addListener(this);
    }

    public CollectionSettingName getStrategyName() {
        return name;
    }

    public boolean collect(CollectionAspectProperties aspect, JoinPoint.StaticPart staticPoint) {
        return enabled;
    }

    @Override
    public int order() {
        return RUN_FIRST;
    }

    public void incrementalUpdate(CollectionSettingName update, Serializable value) {
        if (name.equals(update) && (value instanceof Boolean)) {
            enabled = ((Boolean) value).booleanValue();
        }
    }
}
