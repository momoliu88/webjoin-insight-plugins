/**
 * Copyright (c) 2009-2010 VMware, Inc. All Rights Reserved.
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

package com.ebupt.webjoin.insight.collection;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingName;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsRegistry;
import com.ebupt.webjoin.insight.intercept.plugin.CollectionSettingsUpdateListener;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;



/**
 * Provides common functionality for including/excluding resources according
 * to respective {@link Pattern}s. It registers itself as a {@link CollectionSettingsUpdateListener}
 * so that it can be be updated dynamically. It allows for either an inclusion
 * pattern or an exclusion pattern (or both). If both patterns are active then
 * exclusion &quot;trumps&quot; inclusion if both are active and match the
 * resource. If exclusion pattern set with no inclusion pattern, then it is
 * assumed to be a &quot;match all&quot; state
 */
public abstract class PatternedResourceCollectionFilter implements CollectionSettingsUpdateListener {
	/**
	 * Special pattern value used to indicate that no pattern is used
	 */
	public static final String	NO_PATTERN="none";

	private final CollectionSettingName includedPatternSetting, excludedPatternSetting;

	private final AtomicReference<Pattern>	includedPatternHolder=new AtomicReference<Pattern>(null);
	private final AtomicReference<Pattern>	excludedPatternHolder=new AtomicReference<Pattern>(null);

	/**
	 * @param includedSetting The {@link CollectionSettingName} used to
	 * configure the inclusion {@link Pattern} - may be <code>null</code>
	 * to indicate no such pattern is activated
	 * @param excludedSetting The {@link CollectionSettingName} used to
	 * configure the exclusion {@link Pattern} - may be <code>null</code>
	 * to indicate no such pattern is activated
	 * @throws IllegalStateException if both settings are <code>null</code>
	 * @see #PatternedResourceCollectionFilter(CollectionSettingsRegistry, CollectionSettingName, CollectionSettingName)
	 */
	protected PatternedResourceCollectionFilter(CollectionSettingName includedSetting, CollectionSettingName excludedSetting) {
		this(CollectionSettingsRegistry.getInstance(), includedSetting, excludedSetting);
	}

	/**
	 * @param includedSetting The {@link CollectionSettingName} used to
	 * configure the inclusion {@link Pattern} - may be <code>null</code>
	 * to indicate no such pattern is activated
	 * @param excludedSetting The {@link CollectionSettingName} used to
	 * configure the exclusion {@link Pattern} - may be <code>null</code>
	 * to indicate no such pattern is activated
	 * @param registry The {@link CollectionSettingsRegistry} to register as a
	 * listener - if <code>null</code> then no registration occurs
	 * @throws IllegalStateException if both settings are <code>null</code>
	 * @see CollectionSettingsRegistry#addListener(CollectionSettingsUpdateListener)
	 */
	protected PatternedResourceCollectionFilter(
			CollectionSettingsRegistry registry, CollectionSettingName includedSetting, CollectionSettingName excludedSetting) {
		if ((includedSetting == null) && (excludedSetting == null)) {
			throw new IllegalStateException("No settings provided");
		}

		includedPatternSetting = includedSetting;
		excludedPatternSetting = excludedSetting;

		if (registry != null) {
			registry.addListener(this);
		}
	}

	/**
	 * @param resourcePath The resource path to be checked
	 * @return <code>true</code> if the resource path passes the current
	 * patterns settings
	 * @see #isPatternMatchingEnabled()
	 * @see #matches(CollectionSettingName, Pattern, String) 
	 */
	public boolean accept (String resourcePath) {
		if (!isPatternMatchingEnabled()) {
			return false;
		}
		
		if (!matchesPatterns(resourcePath)) {
			return false;	// debug breakpoint
		}

		return true;
	}

	/**
	 * @return <code>true</code> if pattern matching should occur - by default
	 * it checks if either the inclusion or the exclusion pattern is set.
	 * <B>Note:</B> if <code>false</code> then {@link #accept(String)}
	 * <U>fails</U> regardless of the current patterns
	 */
	public boolean isPatternMatchingEnabled () {
		return (getInclusionPattern() != null) || (getExclusionPattern() != null);
	}

	public final CollectionSettingName getInclusionSetting () {
		return includedPatternSetting;
	}

	public Pattern getInclusionPattern () {
		return includedPatternHolder.get();
	}

	public void setInclusionPattern (String pattern) {
		setInclusionPattern((StringUtil.isEmpty(pattern) || NO_PATTERN.equals(pattern)) ? (Pattern) null : Pattern.compile(pattern));
	}

	public void setInclusionPattern (Pattern pattern) {
		includedPatternHolder.set(pattern);
	}

	public final CollectionSettingName getExclusionSetting () {
		return excludedPatternSetting;
	}

	public Pattern getExclusionPattern () {
		return excludedPatternHolder.get();
	}

	public void setExclusionPattern (String pattern) {
		setExclusionPattern((StringUtil.isEmpty(pattern) || NO_PATTERN.equals(pattern)) ? (Pattern) null : Pattern.compile(pattern));
	}

	public void setExclusionPattern (Pattern pattern) {
		excludedPatternHolder.set(pattern);
	}

    public void incrementalUpdate(CollectionSettingName name, Serializable value) {
    	if ((name == null) || (value == null)) {
    		return;
    	}

    	AtomicReference<Pattern>	refPattern=null;
    	if (ObjectUtil.typedEquals(getInclusionSetting(), name)) {
    		refPattern = includedPatternHolder;
    	} else if (ObjectUtil.typedEquals(getExclusionSetting(), name)) {
    		refPattern = excludedPatternHolder;
    	}

    	if (refPattern != null) {
            Pattern	newPattern=NO_PATTERN.equals(StringUtil.safeToString(value))
            			? null
            			: CollectionSettingsRegistry.getPatternSettingValue(value)
            			;
            Pattern	oldPattern=refPattern.getAndSet(newPattern);
            Logger   LOG=Logger.getLogger(getClass().getName());
            LOG.info("incrementalUpdate(" + name + ") " + oldPattern + " => " + newPattern);
    	}
    }

    /**
     * @param resourcePath The resource path
     * @return <code>true</code> if the resource path is accepted according
     * to the current patterns settings. <B>Note:</B> exclusion &quot;trumps&quot;
     * inclusion if both are active and match the resource.
     * @see #matches(CollectionSettingName, Pattern, String)
     */
    protected boolean matchesPatterns (String resourcePath) {
    	Pattern pattern=getExclusionPattern();
		if ((pattern != null) && matches(getExclusionSetting(), pattern, resourcePath)) {
			return false;	// debug breakpoint
		}

		pattern = getInclusionPattern();
		if ((pattern == null) || matches(getInclusionSetting(), pattern, resourcePath)) {
			return true;	// debug breakpoint
		}

		return false;
    }

    /**
     * @param setting The {@link CollectionSettingName} being checked
     * @param pattern The associated {@link Pattern}
     * @param resourcePath The resource path
     * @return <code>true</code> if the pattern is not <code>null</code>
     * and matches the path
     */
    protected boolean matches (CollectionSettingName setting, Pattern pattern, String resourcePath) {
    	if (pattern == null) {
    		return false;
    	}

    	Matcher	m=pattern.matcher(resourcePath);
    	if (m.matches()) {
    		return true;	// debug breakpoint
    	}

    	return false;
    }

	@Override
	public String toString() {
		return        getInclusionSetting() + ": " + getInclusionPattern()
		     + ", " + getExclusionSetting() + ": " + getExclusionPattern()
		     ;
	}
}
