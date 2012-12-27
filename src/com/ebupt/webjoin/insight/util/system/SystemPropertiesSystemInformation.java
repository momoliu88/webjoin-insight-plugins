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

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;

 

/**
 * Exposes the current system properties as a {@link SystemInformation} 
 */
public class SystemPropertiesSystemInformation
            extends AbstractSystemInformation
            implements NamedPropertySource {
    private static final SystemPropertiesSystemInformation   INSTANCE=new SystemPropertiesSystemInformation();

    public SystemPropertiesSystemInformation() {
        super();
    }

    public static final SystemPropertiesSystemInformation getInstance () {
        return INSTANCE;
    }

    public String getProperty(String name) {
        return System.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return System.getProperty(name, defaultValue);
    }

    public Collection<String> getPropertyNames() {
        return PropertiesUtil.propertiesNames(System.getProperties());
    }

    @Override
	public <A extends Appendable> A appendProperties(A sb) throws IOException {
		return PropertiesUtil.appendProperties(sb, this);
	}

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        Map<String,String>  propsMap=new TreeMap<String, String>();
        for (Map.Entry<?,?> pe : System.getProperties().entrySet()) {
            Object  key=pe.getKey(), value=pe.getValue();
            propsMap.put(String.valueOf(key), String.valueOf(value));
        }
        return propsMap;
    }

    @Override
    public String toString() {
        return super.toString() + System.getProperties();
    }

    /**
     * Uses {@link System#getProperty(String)} on <code>java.version</code>
     * and parses the result
     * @return The current java major version value: 5, 6, 7, etc. - negative
     * value if cannot determine it.
     */
    public static final int getCurrentJavaVersion () {
    	String		javaVersion=System.getProperty("java.version");
    	String[]	comps=javaVersion.split("\\.");
    	if ((ArrayUtil.length(comps) <= 1) || (!StringUtil.isIntegerNumber(comps[1]))) {
    		return (-1);
    	} else {
    		return Integer.parseInt(comps[1]);
    	}
    }
}
