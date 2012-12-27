/**
 * Copyright (c) 2009-2011 VMware.
 * All rights reserved.
 */

package com.ebupt.webjoin.insight.collection.strategies;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.aspectj.lang.JoinPoint;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.ebupt.webjoin.insight.util.ArrayUtil;

/**
 * Organizes and executes a set of CollectionStrategies
 */
public class CollectionStrategyRunner implements
		CollectionSettingsUpdateListener {
	private final Logger log = Logger.getLogger(getClass().getSimpleName());
	private final ConcurrentHashMap<CollectionSettingName, CollectionStrategy> allStrategies = 
			new ConcurrentHashMap<CollectionSettingName, CollectionStrategy>();
	private volatile CollectionStrategy[] execStrategies = new CollectionStrategy[0];
	private final CollectionSettingsRegistry registry;
	private static final CollectionStrategyRunner INSTANCE = new CollectionStrategyRunner();

	private CollectionStrategyRunner() {
		this(CollectionSettingsRegistry.getInstance());
	}

	CollectionStrategyRunner(CollectionSettingsRegistry reg) {
		this(reg, new EndPointOnlyCollectionStrategy(reg),
				new PrefixExcludeCollectionStrategy(reg),
				new DisableAgentCollectionStrategy(reg),
				new AspectManagementCollectionStrategy(reg),
				new IgnoredContextCollectionStrategy(reg),
				new DisablePluginCollectionStrategy(reg));
	}

	CollectionStrategyRunner(CollectionStrategy... strategies) {
		this(CollectionSettingsRegistry.getInstance(), strategies);
	}

	CollectionStrategyRunner(CollectionSettingsRegistry reg,
			CollectionStrategy... strategies) {
		registry = reg;
		reg.addListener(this);

		if (ArrayUtil.length(strategies) > 0) {
			for (CollectionStrategy s : strategies) {
				register(s);
			}
		}
	}

	public static final CollectionStrategyRunner getInstance() {
		return INSTANCE;
	}

	public void register(CollectionStrategy collectionStrategy) {
		allStrategies.put(collectionStrategy.getStrategyName(),
				collectionStrategy);
		if (isStrategyEnabled(collectionStrategy)) {
			enable(collectionStrategy);
		}
	}

	public boolean isStrategyEnabled(CollectionStrategy collectionStrategy) {
		return registry.getBooleanSetting(collectionStrategy.getStrategyName())
				.booleanValue();
	}

	private synchronized void enable(CollectionStrategy collectionStrategy) {
		CollectionStrategy existing = allStrategies.get(collectionStrategy
				.getStrategyName());
		Set<CollectionStrategy> activeStrategies = new HashSet<CollectionStrategy>(
				Arrays.asList(execStrategies));

		if (!activeStrategies.contains(existing)
				&& isStrategyEnabled(collectionStrategy)) {
			activeStrategies.add(existing);
			updateExecStrategies(activeStrategies);
			log.info("Enabled Strategy " + collectionStrategy);
		}
	}

	private void updateExecStrategies(Set<CollectionStrategy> strategies) {
		List<CollectionStrategy> list = new ArrayList<CollectionStrategy>(
				strategies);
		Collections.sort(list);

		execStrategies = list.toArray(new CollectionStrategy[0]);
	}

	private synchronized void disable(CollectionStrategy collectionStrategy) {
		CollectionStrategy existing = allStrategies.get(collectionStrategy
				.getStrategyName());
		Set<CollectionStrategy> activeStrategies = new HashSet<CollectionStrategy>(
				Arrays.asList(execStrategies));

		if (!activeStrategies.remove(existing)
				&& !isStrategyEnabled(collectionStrategy)) {
			log.info("Disabling Strategy " + existing
					+ " - strategy already disabled");
		} else {
			log.info("Disabled Strategy " + existing);
		}
		updateExecStrategies(activeStrategies);
	}

	public boolean collect(CollectionAspectProperties aspect,
			JoinPoint.StaticPart target) {
		for (CollectionStrategy s : execStrategies) {
			if (!s.collect(aspect, target)) {
				return false;
			}
		}
		return true;
	}

	public void incrementalUpdate(CollectionSettingName name, Serializable value) {
		for (CollectionSettingName s : allStrategies.keySet()) {
			if (s.equals(name)) {
				if (((Boolean) value).booleanValue()) {
					enable(allStrategies.get(s));
				} else {
					disable(allStrategies.get(s));
				}
			}
		}
	}
}
