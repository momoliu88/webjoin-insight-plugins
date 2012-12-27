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

package com.ebupt.webjoin.insight.util.system;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;


/**
 * Exposes the underlying environment as a {@link SystemInformation}
 */
public class EnvironmentSystemInformation
        extends AbstractSystemInformation
        implements NamedPropertySource {
    private static final EnvironmentSystemInformation    INSTANCE=new EnvironmentSystemInformation();
    public EnvironmentSystemInformation() {
        super();
    }

    public static final EnvironmentSystemInformation getInstance () {
        return INSTANCE;
    }

    public String getProperty(String name) {
        return getProperty(name, null);
    }

    public String getProperty(String name, String defaultValue) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("No property name provided");
        }

        String  value=System.getenv(name);
        if (value == null) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public Collection<String> getPropertyNames() {
        // return a copy to avoid concurrent modification
        return new TreeSet<String>(System.getenv().keySet());
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        return new TreeMap<String, String>(System.getenv());
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + System.getenv();
    }
}
