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
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;

 

/**
 * This is a pure Java replacement for the SigarSystemInformation
 */
public class RuntimeSystemInformation
        extends AbstractSystemInformation
        implements NamedPropertySource {
    private static final RuntimeSystemInformation   INSTANCE=new RuntimeSystemInformation();
    /**
     * Potentially available properties names (not all may return a value...)
     */
    static final Collection<String> PROPS_NAMES=
            Collections.unmodifiableList(Arrays.asList(
                    ARGUMENTS_KEY,
                    PID_KEY
                    ));
    public static final RuntimeSystemInformation getInstance() {
        return INSTANCE;
    }

    public RuntimeSystemInformation() {
        super();
    }

    public String getProperty(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("No property name");
        }
        
        if (PID_KEY.equals(name)) {
            long    pid=getPid();
            if (pid >= 0L) {
                return String.valueOf(pid);
            }
        } else if (ARGUMENTS_KEY.equals(name)) {
            Collection<String>  args=getInputArguments();
            if (ListUtil.size(args) > 0) {
                return StringFormatterUtils.collectionToDelimitedString(args, " ");
            }
        }
        return null;
    }

    @Override
	public <A extends Appendable> A appendProperties(A sb) throws IOException {
		return PropertiesUtil.appendProperties(sb, this);
	}

    /**
     * @return The current process ID - negative if cannot be inferred
     * @see <A HREF="https://issuetracker.springsource.com/browse/METRICS-2586">METRICS-2586</A>
     */
    public long getPid () {
        RuntimeMXBean   bean=ManagementFactory.getRuntimeMXBean();
        String          name=bean.getName();
        int             splitter=name.indexOf('@');
        if (splitter <= 0) {
            return (-1L);
        }
        
        String  idValue=name.substring(0, splitter);
        try {
            return Long.parseLong(idValue);
        } catch(NumberFormatException e) {  // ignored
            return (-1L);
        }
    }

    /**
     * @return The current invocation arguments
     * @see RuntimeMXBean#getInputArguments()
     */
    public List<String> getInputArguments () {
        RuntimeMXBean   bean=ManagementFactory.getRuntimeMXBean();
        return bean.getInputArguments();
    }

    public Map<String, String> getProperties(Map<String, String> existingProperties) {
        Map<String,String>  result=null;
        for (String name : PROPS_NAMES) {
            String  value=getProperty(name);
            if (StringUtil.isEmpty(value)) {
                continue;
            }
            
            if (result == null) {
                result = new TreeMap<String, String>();
            }
            result.put(name, value);
        }

        if (result == null) {
            return Collections.emptyMap();
        }
        
        return result;
    }

    public String getProperty(String name, String defaultValue) {
        String  value=getProperty(name);
        if (value == null)
            return defaultValue;
        else
            return value;
    }

    public Collection<String> getPropertyNames() {
        Collection<String>  available=new LinkedList<String>();
        for (String name : PROPS_NAMES) {
            String  value=getProperty(name);
            if (StringUtil.isEmpty(value)) {
                continue;
            }
            
            available.add(name);
        }
        
        return available;
    }
}
