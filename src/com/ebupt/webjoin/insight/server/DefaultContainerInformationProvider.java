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

package com.ebupt.webjoin.insight.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;
import com.ebupt.webjoin.insight.util.system.AbstractSystemInformation;

 

/**
 * 
 */
public class DefaultContainerInformationProvider
       extends AbstractSystemInformation
       implements NamedPropertySource {

    /**
     * Optional properties names that may have a value. <B>Note</B> the
     * {@link #getPropertyNames()} returns only the names of properties
     * that actually have a current value
     */
    public static final List<String>    PROPS_NAMES=
            Collections.unmodifiableList(Arrays.asList(
                    CONTAINER_HOME_PATH,
                    CONTAINER_INSTANCE_PATH,
                    CONTAINER_VERSION,
                    CONTAINER_NAME
                ));
    /**
     * A pre-initialized instance
     */
    private static final DefaultContainerInformationProvider    INSTANCE=new DefaultContainerInformationProvider();
    /*
     * NOTE: we do not expose the INSTANCE but rather the proxy since we
     * want a "bean" of ContainerInformationProvider type, whereas this
     * is a SystemInformation one  
     */
    public static final DefaultContainerInformationProvider getInstance () {
        return INSTANCE;
    }

    DefaultContainerInformationProvider() {
        super();
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        Map<String,String>  result=new TreeMap<String, String>();
        for (String name : PROPS_NAMES) {
            String  value=getProperty(name);
            if (StringUtil.isEmpty(value)) {
                continue;
            } else {
                result.put(name, value);
            }
        }

        return result;
    }

    @Override
	public <A extends Appendable> A appendProperties(A sb) throws IOException {
		return PropertiesUtil.appendProperties(sb, this);
	}

    public String getProperty(String name, String defaultValue) {
        String  value=getProperty(name);
        if (StringUtil.isEmpty(value)) {
            return defaultValue;
        } else {
            return value;
        }
    }

    public Collection<String> getPropertyNames() {
        Collection<String>  names=new ArrayList<String>(PROPS_NAMES);   // assume the bes
        for (String prop : PROPS_NAMES) {
            String  value=getProperty(prop);
            if (StringUtil.isEmpty(value)) {
                names.remove(prop);
            }
        }

        return names;
    }

    public String getProperty(String name) {
        if (StringUtil.isEmpty(name)) {
            return null;
        }
        
        ContainerInformationProvider provider=ContainerDetector.getContainerInformationProvider();
        if (CONTAINER_HOME_PATH.equals(name)) {
            return provider.getInstallFolder(); 
        } else if (CONTAINER_INSTANCE_PATH.equals(name)) {
            return provider.getInstanceFolder();
        } else if (CONTAINER_VERSION.equals(name)) {
        	return provider.getContainerVersion();
        } else if (CONTAINER_NAME.equals(name)) {
            return provider.getContainerName();
        } else {    // none of the supported properties
            return null;
        }
    }

}
