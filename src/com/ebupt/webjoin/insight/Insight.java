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

package com.ebupt.webjoin.insight;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.application.ApplicationName;
import com.ebupt.webjoin.insight.intercept.ConfigUtils;
import com.ebupt.webjoin.insight.rabinfingerprint.fingerprint.RabinFingerprintLongWindowed;
import com.ebupt.webjoin.insight.rabinfingerprint.polynomial.Polynomial;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;

/**
 * A central class used for holding Insight related configuration.
 */
public class Insight implements NamedPropertySource {
	//for rabin
	public static final Polynomial poly =  Polynomial.createIrreducible(53);
	public static  RabinFingerprintLongWindowed window ;
	public static final String CONFIG_PROP_CONTEXT_IGNORE_PREFIX = "application.context.ignore.";
	public static final String SYS_PROP_CONTEXT_OVERRIDE_IGNORE = "insight.context.override.ignore";
	public static final String SYS_PROP_ENABLED = "insight.enabled";

	private static final Insight INSTANCE = new Insight();
	private final Set<ApplicationName> ignoredContexts = new TreeSet<ApplicationName>(
			ApplicationName.BY_NAME_COMPARATOR);
	private final Collection<InsightContextIgnoredStateChangeListener> contextChangeListeners = new TreeSet<InsightContextIgnoredStateChangeListener>(
			ObjectUtil.OBJECT_INSTANCE_COMPARATOR);

	private volatile Properties configProps;
	private volatile boolean contextOverrideIgnore;
	private volatile boolean insightEnabled = true;
	private final Logger log = Logger.getLogger(getClass().getName());

	public static final Insight getInstance() {
		return INSTANCE;
	}

	public static final Insight getConfiguredInstance() {
		return getInstance().populateConfigIfNotConfigured();
	}

	Insight() {
		populateSysPropBasedConfig();
		window= new RabinFingerprintLongWindowed(
				poly, 48);
	}

	public Collection<String> getPropertyNames() {
		return PropertiesUtil.propertiesNames(configProps);
	}

	public String getProperty(String name) {
		return getProperty(name, null);
	}

	public String getProperty(String name, String defaultValue) {
		return (configProps == null) ? null : configProps.getProperty(name,
				defaultValue);
	}

	/**
	 * Read the specified configuration file if Insight has not yet been
	 * configured.
	 */
	public Insight readConfigurationIfNotConfigured(File f) {
		if (configProps != null) {
			return this;
		}

		Properties props = readPropsFromFile(f);
		return populateConfig(props);
	}

	private Properties readPropsFromFile(File f) {
		ConfigUtils config = ConfigUtils.getInstance();
		File propsFile = config.getPropsFile();
		try {
			if (FileUtil.compareByCanonicalPath(propsFile, f) == 0) {
				return config.getInsightProperties();
			}
		} catch (IOException e) {
			log.warning(e.getClass().getSimpleName()
					+ " while comparing props file "
					+ propsFile.getAbsolutePath() + " with "
					+ f.getAbsolutePath() + ": " + e.getMessage());
		}

		try {
			return PropertiesUtil.loadFromFile(f);
		} catch (IOException e) {
			log.warning(e.getClass().getSimpleName() + " while reading from ["
					+ f.getAbsolutePath() + "]: " + e.getMessage());
			return new Properties();
		}
	}

	public Insight populateConfigIfNotConfigured() {
		ConfigUtils config = ConfigUtils.getInstance();
		return populateConfigIfNotConfigured(config.getInsightProperties());
	}

	public Insight populateConfigIfNotConfigured(Properties props) {
		if (configProps != null) {
			return this;
		}

		return populateConfig(props);
	}

	public Insight populateConfig(Properties props) {
		populateIgnoredContexts(props);
		this.configProps = props;
		return this;
	}

	private void populateIgnoredContexts(Properties props) {
		for (Map.Entry<Object, Object> prop : props.entrySet()) {
			String key = String.valueOf(prop.getKey());
			if (key.startsWith(CONFIG_PROP_CONTEXT_IGNORE_PREFIX)) {
				setContextIgnored(StringUtil.safeToString(prop.getValue()),
						true);
			}
		}
	}

	private void populateSysPropBasedConfig() {
		contextOverrideIgnore = Boolean.valueOf(
				System.getProperty(SYS_PROP_CONTEXT_OVERRIDE_IGNORE, "false"))
				.booleanValue();
		insightEnabled = Boolean.valueOf(
				System.getProperty(SYS_PROP_ENABLED, "true")).booleanValue();
	}

	/**
	 * @return <code>true</code> if Insight is enabled for monitoring and
	 *         tracing.
	 */
	public boolean isInsightEnabled() {
		return insightEnabled;
	}

	/**
	 * @param l
	 *            The {@link InsightContextIgnoredStateChangeListener} to
	 *            register
	 * @return <code>true</code> if listener successfully registered (
	 *         <code>false</code> if listener already registered)
	 * @throws IllegalArgumentException
	 *             if no listener
	 */
	public boolean addContextStateChangeListener(
			InsightContextIgnoredStateChangeListener l) {
		if (l == null) {
			throw new IllegalArgumentException("No listener");
		}

		synchronized (contextChangeListeners) {
			return contextChangeListeners.add(l);
		}
	}

	/**
	 * @param l
	 *            The {@link InsightContextIgnoredStateChangeListener} to
	 *            un-register
	 * @return <code>true</code> if listener successfully un-registered (
	 *         <code>false</code> if listener not registered to begin with)
	 * @throws IllegalArgumentException
	 *             if no listener
	 */
	public boolean removeContextStateChangeListener(
			InsightContextIgnoredStateChangeListener l) {
		if (l == null) {
			throw new IllegalArgumentException("No listener");
		}

		synchronized (contextChangeListeners) {
			return contextChangeListeners.remove(l);
		}
	}

	public boolean isContextIgnored(String context) {
		return isContextIgnored(ApplicationName
				.valueOf(canonizeContextValue(context)));
	}

	/**
	 * @param applicationName
	 *            The referenced {@link ApplicationName}
	 * @return <code>true</code> if the context with the given name should be
	 *         ignored by Insight for purposes of weaving, tracing, etc.
	 */
	public boolean isContextIgnored(ApplicationName applicationName) {
		if (contextOverrideIgnore) {
			return false;
		}

		synchronized (ignoredContexts) {
			return ignoredContexts.contains(applicationName);
		}
	}

	public void setContextIgnored(String context, boolean ignored) {
		setContextIgnored(
				ApplicationName.valueOf(canonizeContextValue(context)), ignored);
	}

	/**
	 * Marks the provided {@link ApplicationName} as ignored (or not) -
	 * according to the <code>ignored</code> parameter
	 * 
	 * @param appName
	 *            The referenced {@link ApplicationName}
	 * @param ignored
	 *            Whether to ignore it or not
	 * @return <code>true</code> if the current ignored contexts have been
	 *         modified in any way due to this request.
	 * @see #getIgnoredContexts()
	 */
	public boolean setContextIgnored(ApplicationName appName, boolean ignored) {
		if (appName == null) {
			throw new IllegalArgumentException("No application name provided");
		}

		final boolean result;
		synchronized (ignoredContexts) {
			if (ignored) {
				result = ignoredContexts.add(appName);
			} else {
				result = ignoredContexts.remove(appName);
			}
		}

		notifyContextIgnoredStateChange(appName, ignored);
		return result;
	}

	protected List<InsightContextIgnoredStateChangeListener> notifyContextIgnoredStateChange(
			ApplicationName appName, boolean ignored) {
		final List<InsightContextIgnoredStateChangeListener> toNotify;
		// use a copy to avoid locking it while the listeners are called
		synchronized (contextChangeListeners) {
			if (ListUtil.size(contextChangeListeners) <= 0) {
				return Collections.emptyList();
			}

			toNotify = new ArrayList<InsightContextIgnoredStateChangeListener>(
					contextChangeListeners);
		}

		Collection<InsightContextIgnoredStateChangeListener> failed = null;
		for (InsightContextIgnoredStateChangeListener l : toNotify) {
			try {
				l.updatedContextIgnoredState(appName, ignored);
			} catch (Exception e) {
				log.warning("notifyContextIgnoredStateChange(" + appName + ")["
						+ ignored + "]" + " failed ("
						+ e.getClass().getSimpleName() + ")" + " on call to "
						+ l.getClass().getSimpleName() + ": " + e.getMessage());
				if (failed == null) {
					failed = new LinkedList<InsightContextIgnoredStateChangeListener>();
				}

				failed.add(l);
			}
		}

		if (ListUtil.size(failed) > 0) {
			toNotify.removeAll(failed);
		}

		return toNotify;
	}

	/**
	 * @return An (unmodifiable) {@link Set} of the {@link ApplicationName}-s
	 *         currently marked to be ignored
	 */
	public Set<ApplicationName> getIgnoredContexts() {
		synchronized (ignoredContexts) {
			if (ignoredContexts.isEmpty()) {
				return Collections.emptySet();
			} else {
				return Collections.unmodifiableSet(ignoredContexts);
			}
		}
	}

	// Normalize to "" from "/" based on HttpServletRequest#getContextPath
	static String canonizeContextValue(String context) {
		if ("/".equals(context)) {
			return "";
		} else {
			return context;
		}
	}
}
