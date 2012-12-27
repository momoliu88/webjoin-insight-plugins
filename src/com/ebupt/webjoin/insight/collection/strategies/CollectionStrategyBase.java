/**
 * Copyright (c) 2009-2011 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;

/**
 * Default implementation of the CollectionStrategy which does nothing.
 */
public abstract class CollectionStrategyBase implements CollectionStrategy {

    public int order() { return RUN_LAST;}

    public int compareTo(CollectionStrategy o) {
        return order() - o.order();
    }

    @Override
    public int hashCode() {
        return getStrategyName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return getStrategyName().equals(o);
    }
}
