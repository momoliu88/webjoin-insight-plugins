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

package com.ebupt.webjoin.insight.intercept.plugin;

import java.io.Serializable;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;


/**
 * Defines a setting associated with a plugin or general collection strategy
 */
public class CollectionSettingName implements Serializable {
    private static final long serialVersionUID = 1709026199355118106L;

    public static final String GLOBAL_COLLECTION_STRATEGY = "global";
    public static final String COLLECTION_STRATEGY_PREFIX = "insight.collection";
    private static final Pattern COLLECTION_STRATEGY_KEY_MATCHER = Pattern.compile(COLLECTION_STRATEGY_PREFIX + "\\.([^.]+)\\.(.+$)");
    private final String name;
    private final boolean informational;

    /**
     * Features may or may not be associated with a particular plugin
     */
    private final String plugin;

    private final String description;
    // we can calculate these upon construction since the values they depend on are 'final'
    private volatile transient String key, settingKey;
    private volatile transient Integer hashValue;

    public CollectionSettingName(String settingName, String pluginName, String settingDescription) {
        this(settingName, pluginName, settingDescription, false);
    }

    /**
     * Create a new CollectionSettingName
     * @param settingName the name of the setting
     * @param pluginName the plugin the setting is associated with
     * @param settingDescription the description of the setting
     * @param infoSetting
     */
    public CollectionSettingName(String settingName, String pluginName, String settingDescription, boolean infoSetting) {
        if (StringUtil.isEmpty(settingName)) {
            throw new IllegalStateException("No setting name provided");
        }
        if (StringUtil.isEmpty(pluginName)) {
            throw new IllegalStateException("No plugin name provided");
        }

        this.name = settingName;
        this.plugin = pluginName;
        this.key = pluginName + "." + settingName;
        this.settingKey = COLLECTION_STRATEGY_PREFIX + "." + this.key;
        this.hashValue = Integer.valueOf((31 * ObjectUtil.hashCode(settingName)) + ObjectUtil.hashCode(pluginName));
        this.description = (settingDescription == null) ? "" : settingDescription;
        this.informational = infoSetting;
    }

    public CollectionSettingName(String settingName, String pluginName) {
        this(settingName, pluginName, "");
    }

    public CollectionSettingName(String settingName) {
        this(settingName, GLOBAL_COLLECTION_STRATEGY, "");
    }

    public String getName() {
        return name;
    }

    public String getPlugin() {
        return plugin;
    }

    public String getDescription() {
        return description;
    }

    public boolean isInformational() {
        return informational;
    }

    public String getCollectionSettingsKey() {
        if (settingKey == null) {
            settingKey = COLLECTION_STRATEGY_PREFIX + "." + getKey();
        }
        
        return settingKey;
    }

    public String getKey() {
        if (key == null) {
            key = getPlugin() + "." + getName();
        }
        return key;
    }

    /**
     * Creates a collection setting name from the given key, which must be
     * prefixed with "insight.collection" - otherwise null is returned.
     */
    public static CollectionSettingName getCollectionSettings(String key) {
        String plugin;
        String name;
        Matcher matcher = COLLECTION_STRATEGY_KEY_MATCHER.matcher(key);
        if (matcher.matches()) {
            plugin = matcher.group(1);
            name = matcher.group(2);
            return new CollectionSettingName(name, plugin);
        }
        return null;
    }

    /**
     * Compares using {@link CollectionSettingName#getKey()} value
     */
    public static final Comparator<CollectionSettingName>   BY_KEY_COMPARATOR=
            new Comparator<CollectionSettingName>() {
                public int compare(CollectionSettingName o1, CollectionSettingName o2) {
                    return StringUtil.safeCompare((o1 == null) ? null : o1.getKey(), (o2 == null) ? null : o2.getKey()); 
                }
                
        };

    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (this == o)
            return true;
        if (getClass() != o.getClass())
            return false;

        CollectionSettingName other = (CollectionSettingName) o;
        return ObjectUtil.typedEquals(getName(), other.getName())
            && ObjectUtil.typedEquals(getPlugin(), other.getPlugin())
            ;
    }

    @Override
    public int hashCode() {
        if (hashValue == null) {
            hashValue = Integer.valueOf((31 * ObjectUtil.hashCode(getName())) + ObjectUtil.hashCode(getPlugin()));
        }
        
        return hashValue.intValue();
    }

    @Override
    public String toString() {
        return "CollectionStrategy{" +
                "name='" + getName() + '\'' +
                ", plugin='" + getPlugin() + '\'' +
                '}';
    }
}
