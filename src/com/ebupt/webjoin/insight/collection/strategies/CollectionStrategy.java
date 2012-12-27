package com.ebupt.webjoin.insight.collection.strategies;

 import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;

/**
 * Defines a CollectionStrategy that can be a cross-cutting concern
 * at different contexts of trace collection. This is complementary
 * to the CollectionSettings. In many cases plugins can use
 * CollectionSettings directly without implementing a full
 * CollectionStrategy.
 *
 */
public interface CollectionStrategy extends Comparable<CollectionStrategy> {
    final static int RUN_FIRST = 1;
    final static int RUN_LAST = 100;

    /**
     * The name of the strategy, used to determine, based on the configuration,
     * whether a strategy should run.
     */
    abstract CollectionSettingName getStrategyName();

    /**
     * Return true if the pointcut indicated by the collection aspect and staticPoint should
     * be collected
     * @param aspect thisAspectInstance, available in Aspectj 1.6.12.M2
     * @param staticPoint thisJoinPointStaticPart
     */
    abstract boolean collect(CollectionAspectProperties aspect, JoinPoint.StaticPart staticPoint);

    /**
     * The relative order in which this CollectionStrategy should run. Lower
     * numbers mean that the strategy should run earlier.
     * @return
     */
    abstract int order();
}
