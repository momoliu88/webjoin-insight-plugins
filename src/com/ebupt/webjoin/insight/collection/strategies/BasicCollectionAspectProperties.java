/**
 * Copyright (c) 2009-2011 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;


/**
 * Basic Information about a Collection Aspect
 */
public class BasicCollectionAspectProperties implements CollectionAspectProperties {
    private final boolean endpoint;
    private final String plugin;

    public BasicCollectionAspectProperties(boolean endpointValue, String pluginName) {
        this.endpoint = endpointValue;
        this.plugin = pluginName;
    }

    public boolean isEndpoint() {
        return endpoint;
    }

    public String getPluginName() {
        return plugin;
    }

    @Override
    public String toString() {
        return getPluginName() + "[endpoint=" + isEndpoint() + "]";
    }
}
 