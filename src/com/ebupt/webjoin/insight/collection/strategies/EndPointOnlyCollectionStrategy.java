/**
 * Copyright (c) 2009-2011 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.InterceptConfiguration;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.ebupt.webjoin.insight.intercept.trace.FrameBuilder;
import com.ebupt.webjoin.insight.intercept.trace.NullFrameBuilderCallback;
import com.ebupt.webjoin.insight.intercept.util.PercentageTracker;

 


/**
 * CollectionStrategy which excludes frames which are not potiential endpoints as
 * indicated by the aspect.
 */
public class EndPointOnlyCollectionStrategy extends CollectionStrategyBase implements CollectionSettingsUpdateListener {
    public static final String ENDPOINT_ONLY_COLLECTION_PERCENT_CONFIG_KEY_PART = "percent";
    public static final String ENDPOINT_ONLY_COLLECTION_STRATEGY_KEY_PART =  FrameBuilder.HINT_COLLECT_ONLY_ENDPOINTS;
    public static final String ENDPOINT_ONLY_COLLECTION_PERCENT_KEY = FrameBuilder.HINT_COLLECT_ONLY_ENDPOINTS + ".percent";
    public static final String ENDPOINT_ONLY_COLLECTION_PERCENT_CONFIG_KEY
            = CollectionSettingName.COLLECTION_STRATEGY_PREFIX + "." + ENDPOINT_ONLY_COLLECTION_PERCENT_KEY;

    public static final CollectionSettingName ENDPOINT_ONLY_COLLECTION_STRATEGY
            = new CollectionSettingName("enabled",
            ENDPOINT_ONLY_COLLECTION_STRATEGY_KEY_PART,
            "Collect a subset of traces which only contain potential end-points. " +
                    "The percentage of full traces can be configured with "
                    + ENDPOINT_ONLY_COLLECTION_PERCENT_CONFIG_KEY);

    public static final CollectionSettingName PERCENT_COLLECTION_SETTING
            = new CollectionSettingName(ENDPOINT_ONLY_COLLECTION_PERCENT_CONFIG_KEY_PART,
            ENDPOINT_ONLY_COLLECTION_STRATEGY_KEY_PART, "Percentage of traces which should include non-endpoints");

    private final PercentageTracker fullCollectionRatioTracker
            = new PercentageTracker("fullCollectionRatio");

    private final CollectionSettingsRegistry registry;
    private final FrameBuilder frameBuilder = InterceptConfiguration.getInstance().getFrameBuilder();

    public EndPointOnlyCollectionStrategy() {
        this(CollectionSettingsRegistry.getInstance());
    }

    public EndPointOnlyCollectionStrategy(CollectionSettingsRegistry reg) {
        registry = reg;
        fullCollectionRatioTracker.setTrackedRatio(Integer.parseInt(System.getProperty(ENDPOINT_ONLY_COLLECTION_PERCENT_CONFIG_KEY, "100")));
        registry.register(ENDPOINT_ONLY_COLLECTION_STRATEGY, Boolean.TRUE);
        registry.register(PERCENT_COLLECTION_SETTING, Integer.valueOf(fullCollectionRatioTracker.getCurrentRatio()));
        InterceptConfiguration.getInstance().addFrameBuilderCallbacks(new FrameBuilderListener(frameBuilder, fullCollectionRatioTracker));
    }

    public CollectionSettingName getPercentCollectionSetting() {
        return PERCENT_COLLECTION_SETTING;
    }

    public CollectionSettingName getStrategyName() {
        return ENDPOINT_ONLY_COLLECTION_STRATEGY;
    }

    public int getPercentageFullTraces()  {
        return fullCollectionRatioTracker.getTrackedRatio();
    }

    public boolean enabled() {
        return registry.getBooleanSetting(ENDPOINT_ONLY_COLLECTION_STRATEGY).booleanValue();
    }

    public final boolean collect(CollectionAspectProperties aspect, JoinPoint.StaticPart ignored) {
        if (aspect.isEndpoint()) {
            return true;
        }
        // check hint
        Boolean hint = frameBuilder.getHint(FrameBuilder.HINT_COLLECT_ONLY_ENDPOINTS, Boolean.class);
        return hint == null || hint.equals(FALSE);
    }

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
        if (!PERCENT_COLLECTION_SETTING.equals(name)) {
            return;
        }
        Integer updatedValue = (Integer)value;
        if (updatedValue.intValue() != fullCollectionRatioTracker.getTrackedRatio()) {
                fullCollectionRatioTracker.reset();
                fullCollectionRatioTracker.setTrackedRatio(updatedValue.intValue());
        }
    }

    @Override
    public int order() {
        return CollectionStrategy.RUN_FIRST;
    }

    public PercentageTracker getFullCollectionRatioTracker() {
        return fullCollectionRatioTracker;
    }

    public FrameBuilder getFrameBuilder() {
        return frameBuilder;
    }

    public static class FrameBuilderListener extends NullFrameBuilderCallback {
        private final FrameBuilder frameBuilder;
        private final PercentageTracker tracker;

        public FrameBuilderListener(FrameBuilder builder, PercentageTracker pctTracker) {
            this.frameBuilder = builder;
            this.tracker = pctTracker;
        }

        public List<FrameBuilderEvent> listensTo() {
            return Arrays.asList(FrameBuilderEvent.ROOT_ENTER);
        }

        @Override
        public final void enterRootFrame() {
            if (!tracker.isValueProcessingAllowed()) {
                frameBuilder.setHint(FrameBuilder.HINT_COLLECT_ONLY_ENDPOINTS, TRUE);
                frameBuilder.setHint(FrameBuilder.HINT_OPERATION_COLLECT_LEVEL, FrameBuilder.OperationCollectionLevel.LOW);
            }
        }

    }
}
