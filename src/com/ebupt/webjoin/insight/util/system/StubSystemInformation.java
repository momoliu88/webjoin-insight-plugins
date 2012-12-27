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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;



public class StubSystemInformation
        extends AbstractSystemInformation
        implements NamedPropertySource {
    private Map<String, String> props = new HashMap<String, String>();
    
    public StubSystemInformation(Map<String, String> props) {
        this.props.putAll(props);
    }

    public Collection<String> getPropertyNames() {
        // avoid concurrent modifications
        return new TreeSet<String>(props.keySet());
    }

    public String getProperty(String name) {
        return getProperty(name, null);
    }

    @Override
	public <A extends Appendable> A appendProperties(A sb) throws IOException {
		return PropertiesUtil.appendProperties(sb, this);
	}

    public String getProperty(String name, String defaultValue) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("No property name provided");
        }

        String  value=props.get(name);
        if (value == null) {
            return defaultValue;
        }
        
        return value;
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        return Collections.unmodifiableMap(props);
    }
}
