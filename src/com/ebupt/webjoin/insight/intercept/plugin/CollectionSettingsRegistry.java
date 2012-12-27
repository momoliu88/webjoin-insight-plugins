package com.ebupt.webjoin.insight.intercept.plugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.ebupt.webjoin.insight.intercept.util.KeyValPair;
import com.ebupt.webjoin.insight.util.*;
import com.ebupt.webjoin.insight.util.props.*;

/**
 * Manages a Set of Collection Settings as Integers, Booleans, or Strings. This
 * class is designed to be accessible from the trace intercept
 */
public class CollectionSettingsRegistry {
	/*
	 * Delay the actual initialization as late as possible so as to allow the
	 * dispatch core (if any) to have best chance of using a registered
	 * InsightAgentPluginsHelper
	 */
	private static final class LazyFieldHolder {
		private static final String DISPATCHER_REGISTRY_CLASS = "com.ebupt.webjoin.insight.intercept.plugin.DispatcherCollectionSettingsRegistry";
		private static final CollectionSettingsRegistry INSTANCE = resolveInstance();

		private static CollectionSettingsRegistry resolveInstance() {
			try {
				ClassLoader cl = ClassUtil
						.getDefaultClassLoader(CollectionSettingsRegistry.class);
				if (ClassUtil.isPresent(DISPATCHER_REGISTRY_CLASS, cl)) {
					Class<?> clazz = ClassUtil.loadClassByName(cl,
							DISPATCHER_REGISTRY_CLASS);
					return (CollectionSettingsRegistry) clazz.newInstance();
				}
			} catch (Throwable t) {
				Logger LOG = Logger.getLogger(LazyFieldHolder.class.getName());
				LOG.log(Level.SEVERE, "Failed (" + t.getClass().getSimpleName()
						+ ")" + " to instantiate: " + t.getMessage(), t);
			}

			return new CollectionSettingsRegistry();
		}
	}

	private final ConcurrentHashMap<CollectionSettingName, Serializable> collectionSettings = new ConcurrentHashMap<CollectionSettingName, Serializable>();
	private final Collection<CollectionSettingsUpdateListener> listeners = new LinkedList<CollectionSettingsUpdateListener>();

	// TODO: We may want to store additional data about the setting in the
	// registry, such as the authoritative
	// description or the conversion strategy

	public CollectionSettingsRegistry() {
		super();
	}

	@SuppressWarnings("synthetic-access")
	public static final CollectionSettingsRegistry getInstance() {
		return LazyFieldHolder.INSTANCE;
	}

	/**
	 * Configures settings from a Properties Object. Only Boolean, Long, and
	 * String are supported at this time.
	 */
	public void configure(Properties prop) {
		doConfigure(PropertiesUtil.toPropertySource(prop));
	}

	public void configure(NamedPropertySource prop) {
		doConfigure(prop);
	}

	// returns a Map of the CHANGED values
	protected Map<CollectionSettingName, KeyValPair<Serializable, Serializable>> doConfigure(
			NamedPropertySource prop) {
		Map<CollectionSettingName, ? extends Serializable> valuesMap = fromPropertySource(prop);
		if (MapUtil.size(valuesMap) <= 0) {
			return Collections.emptyMap();
		}

		Map<CollectionSettingName, KeyValPair<Serializable, Serializable>> result = new TreeMap<CollectionSettingName, KeyValPair<Serializable, Serializable>>(
				CollectionSettingName.BY_KEY_COMPARATOR);
		for (Map.Entry<CollectionSettingName, ? extends Serializable> ve : valuesMap
				.entrySet()) {
			CollectionSettingName cs = ve.getKey();
			Serializable value = ve.getValue();
			Serializable existing = changeCollectionSetting(cs, value);
			if (ObjectUtil.typedEquals(value, existing)) {
				continue;
			}

			result.put(cs, new KeyValPair<Serializable, Serializable>(existing,
					value));
		}

		return result;
	}

	/**
	 * @param prop
	 *            A {@link NamedPropertySource} to use to extract the settings
	 * @return A {@link Map} of the extracted {@link CollectionSettingName}-s
	 *         and their associated {@link Serializable} values
	 * @throws IllegalStateException
	 *             if same setting repeated
	 */
	public static Map<CollectionSettingName, Serializable> fromPropertySource(
			NamedPropertySource prop) throws IllegalStateException {
		Map<CollectionSettingName, Serializable> result = null;
		for (String key : prop.getPropertyNames()) {
			CollectionSettingName cs = CollectionSettingName
					.getCollectionSettings(key);
			if (cs == null) {
				continue;
			}

			String setting = prop.getProperty(key);
			Serializable value = getTyped(setting);
			if (result == null) {
				result = new TreeMap<CollectionSettingName, Serializable>(
						CollectionSettingName.BY_KEY_COMPARATOR);
			}

			Serializable prev = result.put(cs, value);
			if (prev != null) {
				throw new IllegalStateException("Multiple settings for " + cs
						+ ": " + value + " / " + prev);
			}
		}

		if (result == null) {
			return Collections.emptyMap();
		} else {
			return result;
		}
	}

	/**
	 * <P>
	 * Convert the UI type into a {@link Serializable} value which can be used
	 * as a collection setting value. The conversion is as follows:
	 * </P>
	 * </BR>
	 * <UL>
	 * <LI>
	 * If all digits, then converted to a {@link Long}</LI>
	 * 
	 * <LI>
	 * If &quot;<code>true</code>&quot; or &quot;<code>false</code>&quot; then
	 * converted into a {@link Boolean}</LI>
	 * 
	 * <LI>
	 * Otherwise, return the original {@link String} value</LI>
	 * </UL>
	 * 
	 * @param value
	 *            The {@link String} value to be converted - may not be
	 *            <code>null</code>
	 * @return The &quot;best&quot; {@link Serializable} value
	 */
	public static Serializable getTyped(String value) {
		if (value == null) {
			throw new IllegalArgumentException("Null(s) not allowed");
		}

		if (StringUtil.isIntegerNumber(value)) {
			try {
				return Long.valueOf(value);
			} catch (NumberFormatException e) {
				// swallow
				return value;
			}
		}

		if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
			return Boolean.valueOf(value);
		}

		return value;
	}

	/**
	 * <P>
	 * Extract an <code>int</code> setting value using the following logic:
	 * </P>
	 * </BR>
	 * <UL>
	 * <LI>
	 * If the value is a {@link Number} then return its
	 * {@link Number#intValue()}</LI>
	 * 
	 * <LI>
	 * If the value is a {@link String} then return its
	 * {@link Integer#parseInt(String)}</LI>
	 * 
	 * <LI>
	 * Otherwise, throw {@link UnsupportedOperationException}</LI>
	 * </UL>
	 * 
	 * @param value
	 *            The registered {@link Serializable} value
	 * @return The <code>int<code> value
	 * @throws UnsupportedOperationException
	 *             if the parameter is not one of the supported types
	 * @throws NumberFormatException
	 *             If the value is a an invalid numerical {@link String}
	 */
	public static int getIntegerSettingValue(Serializable value)
			throws UnsupportedOperationException, NumberFormatException {
		if (value instanceof Number) {
			return ((Number) value).intValue();
		} else if (value instanceof String) {
			return Integer.parseInt((String) value);
		} else {
			throw new UnsupportedOperationException(
					"Cannot convert to integer: " + value);
		}
	}

	/**
	 * <P>
	 * Extract a <code>long</code> setting value using the following logic:
	 * </P>
	 * </BR>
	 * <UL>
	 * <LI>
	 * If the value is a {@link Number} then return its
	 * {@link Number#longValue()}</LI>
	 * 
	 * <LI>
	 * If the value is a {@link String} then return its
	 * {@link Long#parseLong(String)}</LI>
	 * 
	 * <LI>
	 * Otherwise, throw {@link UnsupportedOperationException}</LI>
	 * </UL>
	 * 
	 * @param value
	 *            The registered {@link Serializable} value
	 * @return The <code>long<code> value
	 * @throws UnsupportedOperationException
	 *             if the parameter is not one of the supported types
	 * @throws NumberFormatException
	 *             If the value is a an invalid numerical {@link String}
	 */
	public static long getLongSettingValue(Serializable value)
			throws UnsupportedOperationException, NumberFormatException {
		if (value instanceof Number) {
			return ((Number) value).longValue();
		} else if (value instanceof String) {
			return Long.parseLong((String) value);
		} else {
			throw new UnsupportedOperationException("Cannot convert to long: "
					+ value);
		}
	}

	/**
	 * <P>
	 * Extract a <code>boolean</code> setting value using the following logic:
	 * </P>
	 * </BR>
	 * <UL>
	 * <LI>
	 * If the value is a {@link Boolean} then return its
	 * {@link Boolean#booleanValue()}</LI>
	 * 
	 * <LI>
	 * If the value is a {@link String} then return its <code>true</code>/
	 * <code>false</code> value (case <U>sensitive</U> match)</LI>
	 * 
	 * <LI>
	 * Otherwise, throw {@link UnsupportedOperationException}</LI>
	 * </UL>
	 * 
	 * @param value
	 *            The registered {@link Serializable} value
	 * @return The <code>boolean<code> value
	 * @throws UnsupportedOperationException
	 *             if the parameter is not one of the supported types
	 */
	public static boolean getBooleanSettingValue(Serializable value)
			throws UnsupportedOperationException {
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		} else if (value instanceof String) {
			if ("true".equals(value)) {
				return true;
			} else if ("false".equals(value)) {
				return false;
			}
		}

		// none of the above
		throw new UnsupportedOperationException("Cannot convert boolean: "
				+ value);
	}

	/**
	 * <P>
	 * Extract a {@link Level} setting value using the following logic:
	 * </P>
	 * </BR>
	 * <UL>
	 * <LI>
	 * If the value is a {@link Level} then return it</LI>
	 * 
	 * <LI>
	 * If the value is a {@link String} then return its
	 * {@link Level#parse(String)} (case <U>sensitive</U> match)</LI>
	 * 
	 * <LI>
	 * Otherwise, throw {@link UnsupportedOperationException}</LI>
	 * </UL>
	 * 
	 * @param value
	 *            The registered {@link Serializable} value
	 * @return The {@link Level} value
	 * @throws UnsupportedOperationException
	 *             if the parameter is not one of the supported types
	 * @throws IllegalArgumentException
	 *             if {@link String} argument provided to
	 *             {@link Level#parse(String)} cannot be converted to a valid
	 *             {@link Level}
	 */
	public static Level getLogLevelSetting(Serializable value)
			throws UnsupportedOperationException, IllegalArgumentException {
		if (value instanceof Level) {
			return (Level) value;
		} else if (value instanceof String) {
			return Level.parse((String) value);
		}

		// none of the above
		throw new UnsupportedOperationException("Cannot convert level: "
				+ value);
	}

	/**
	 * <P>
	 * Extract a {@link Pattern} setting value using the following logic:
	 * </P>
	 * </BR>
	 * <UL>
	 * <LI>
	 * If the value is a {@link Pattern} then return it</LI>
	 * 
	 * <LI>
	 * If the value is a {@link String} then return its
	 * {@link Pattern#compile(String)} (case <U>sensitive</U> match)</LI>
	 * 
	 * <LI>
	 * Otherwise, throw {@link UnsupportedOperationException}</LI>
	 * </UL>
	 * 
	 * @param value
	 *            The registered {@link Serializable} value
	 * @return The {@link Pattern} value
	 * @throws UnsupportedOperationException
	 *             if the parameter is not one of the supported types
	 * @throws PatternSyntaxException
	 *             if {@link String} argument provided to
	 *             {@link Pattern#compile(String)} cannot be converted to a
	 *             valid pattern
	 */
	public static Pattern getPatternSettingValue(Serializable value)
			throws UnsupportedOperationException, PatternSyntaxException {
		if (value instanceof String) {
			return Pattern.compile((String) value);
		} else if (value instanceof Pattern) {
			return (Pattern) value;
		}

		throw new UnsupportedOperationException("Cannot convert pattern:"
				+ value);
	}

	/**
	 * Adds a new setting to the registry only if it does not already exist
	 */
	public void register(CollectionSettingName cs, Serializable value) {
		Object obj = collectionSettings.putIfAbsent(cs, value);
		if (obj == null) {
			updateListeners(cs, value);
		} else {
			// The setting is already registered. This can occur if
			// the setting is loaded up from a configuration file before the
			// plugin or collection strategy using this setting is able to set
			// the
			// value. Pass the existing value to listeners.
			updateListeners(cs, (Serializable) obj);
		}
	}

	public void set(CollectionSettingName cs, Serializable value) {
		changeCollectionSetting(cs, value);
	}

	public Serializable get(CollectionSettingName cs) {
		return collectionSettings.get(cs);
	}

	public Boolean getBooleanSetting(CollectionSettingName cs) {
		return (Boolean) collectionSettings.get(cs);
	}

	public void disableBoolean(CollectionSettingName cs) {
		changeCollectionSetting(cs, Boolean.FALSE);
	}

	public void enableBoolean(CollectionSettingName cs) {
		changeCollectionSetting(cs, Boolean.TRUE);
	}

	public Map<CollectionSettingName, Serializable> getCollectionSettings() {
		return new HashMap<CollectionSettingName, Serializable>(
				collectionSettings);
	}

	public void addListener(CollectionSettingsUpdateListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	/**
	 * Updates a specified listener with the current values (if any) of the
	 * specific requested settings
	 * 
	 * @param listener
	 *            The {@link CollectionSettingsUpdateListener} to be updated
	 * @param settings
	 *            The {@link CollectionSettingName} whose values are requested
	 * @return A {@link Map} of all the settings that were updated by the
	 *         listener and their associated value(s)
	 * @throws IllegalArgumentException
	 *             if no listener instance provided
	 * @see #updateListenerSettings(CollectionSettingsUpdateListener,
	 *      Collection)
	 */
	public Map<CollectionSettingName, Serializable> updateListenerSettings(
			CollectionSettingsUpdateListener listener,
			CollectionSettingName... settings) {
		return updateListenerSettings(
				listener,
				(ArrayUtil.length(settings) <= 0) ? Collections
						.<CollectionSettingName> emptyList() : Arrays
						.asList(settings));
	}

	/**
	 * Updates a specified listener with the current values (if any) of the
	 * specific requested settings
	 * 
	 * @param listener
	 *            The {@link CollectionSettingsUpdateListener} to be updated
	 * @param settings
	 *            The {@link CollectionSettingName} whose values are requested
	 * @return A {@link Map} of all the settings that were updated by the
	 *         listener and their associated value(s)
	 * @throws IllegalArgumentException
	 *             if no listener instance provided
	 */
	public Map<CollectionSettingName, Serializable> updateListenerSettings(
			CollectionSettingsUpdateListener listener,
			Collection<CollectionSettingName> settings) {
		if (listener == null) {
			throw new IllegalArgumentException("updateListenerSettings("
					+ settings + ") no listener");
		}

		if (ListUtil.size(settings) <= 0) {
			return Collections.emptyMap();
		}

		Map<CollectionSettingName, Serializable> result = null;
		for (CollectionSettingName name : settings) {
			Serializable value = get(name);
			if (value == null) {
				continue;
			}

			listener.incrementalUpdate(name, value);
			if (result == null) {
				result = new TreeMap<CollectionSettingName, Serializable>(
						CollectionSettingName.BY_KEY_COMPARATOR);
			}

			result.put(name, value);
		}

		if (result == null) {
			return Collections.emptyMap();
		} else {
			return result;
		}
	}

	// returns the previous value (null if none)
	public Serializable changeCollectionSetting(CollectionSettingName cs,
			Serializable value) {
		Serializable existing = collectionSettings.get(cs);
		if ((existing != null)
				&& (!value.getClass().equals(existing.getClass()))) {
			System.out.println(cs.toString() + " existing!");
		}
		collectionSettings.put(cs, value);
		updateListeners(cs, value);
		return existing;
	}

	// returns number of updated listeners
	private int updateListeners(CollectionSettingName name, Serializable value) {
		final Collection<CollectionSettingsUpdateListener> listCopy;
		// NOTE: we use a copy in order to avoid ConcurrentModificationException
		synchronized (listeners) {
			if (listeners.isEmpty()) {
				return 0;
			}

			listCopy = new ArrayList<CollectionSettingsUpdateListener>(
					listeners);
		}

		for (CollectionSettingsUpdateListener l : listCopy) {
			l.incrementalUpdate(name, value);
		}

		return listCopy.size();
	}

	// For JUnit Tests
	public void clearListeners() {
		synchronized (listeners) {
			listeners.clear();
		}
	}
}
