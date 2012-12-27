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

package com.ebupt.webjoin.insight.application;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;

import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.resource.ResourceName;
import com.ebupt.webjoin.insight.resource.ResourceNames;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
 

public class ApplicationName implements ResourceName, Serializable, Comparable<ApplicationName> {
    private static final long serialVersionUID = 8288825407637696601L;
    private static final Map<String,ApplicationName> applicationsMap=
        Collections.synchronizedMap(new WeakHashMap<String,ApplicationName>());

    public static final String  UNKNOWN_APPLICATION_NAME="unknown";
    public static final ApplicationName UNKOWN_APPLICATION = ApplicationName.valueOf(UNKNOWN_APPLICATION_NAME);
    /**
     * Name used for the root application name
     */
    public static final String  ROOT_APPLICATION_NAME="ROOT";

    private String name;
    private String formattedName;

    /**
     * Constructor and Setter Methods package scope to allow for mutability in deserialization
     */
    ApplicationName() {
        super();
    }

    private ApplicationName(String n) {
        if (n == null) {
            throw new NullPointerException("Null name N/A");
        }
        this.name = n;
        if (UNKNOWN_APPLICATION_NAME.equals(n)) {
            this.formattedName = UNKNOWN_APPLICATION_NAME;
        }
        else {
            int pos = n.lastIndexOf(CONTEXT_NAME_SEPARATOR);
            String appName = pos < 0 ? n : n.substring(pos + 1);
            formattedName = ROOT_APPLICATION_NAME.equals(appName) ? "/" : "/" + appName;
        }
    }

    /**
     * Character used to separate the host name from the content name
     * in the compound name
     * @see #valueOf(String, String)
     */
    public static final char    CONTEXT_NAME_SEPARATOR='|';

    /**
     * Character used to separate application context paths within the application
     * @see #sanitize(String) 
     */
    public static final char    CONTEXT_PATH_SEPARATOR='/';

    /**
     * Used to build an {@link ApplicationName} from a host and a context.
     * @param host The extracted host name
     * @param context The extracted context name
     * @return The compound {@link ApplicationName} using the {@link #CONTEXT_NAME_SEPARATOR}
     * to separate between the 2 components
     * @throws IllegalArgumentException if no/empty host
     */
    public static ApplicationName valueOf(String host, String context) throws IllegalArgumentException {
        if (StringUtil.isEmpty(host)) {
            throw new IllegalArgumentException("No host provided for context=" + context);
        }

        String          sanContext=sanitize(context);
        StringBuilder   id=new StringBuilder(host.length() + 1 /* separator */ + sanContext.length())
                                .append(host)
                                .append(CONTEXT_NAME_SEPARATOR)
                                .append(sanContext);
        return valueOf(id.toString());
    }

    /**
     * @param value Original context value
     * @return The context value after removing any {@link #CONTEXT_PATH_SEPARATOR}-s
     * in it. If the result is empty the returns {@link #ROOT_APPLICATION_NAME}
     */
    public static String sanitize(String value) {
        String  name = (value == null) ? "" : StringUtil.removeAllCharacterOccurrences(value, CONTEXT_PATH_SEPARATOR);
        if (StringUtil.isEmpty(name)) {
            return ROOT_APPLICATION_NAME;
        }
        return name;
    }

    /*
     * This ensures that we keep using the same reference to after deserialization.
     */    
    public Object readResolve() throws ObjectStreamException {
        if (StringUtil.isEmpty(name)) {
            throw new StreamCorruptedException("No name");
        }
        return valueOf(name);
    }

    public static ApplicationName valueOf(String str) {
        ApplicationName appName=applicationsMap.get(str);
        if (appName == null) {
            appName = new ApplicationName(str);
            applicationsMap.put(str, appName);
        }

        return appName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getFormatted() {
        return formattedName;
    }

    void setName(String n) {
        this.name = n;
    }

    void setFormattedName(String fmtName) {
        this.formattedName = fmtName;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return ObjectUtil.hashCode(getName());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;

        ApplicationName other = (ApplicationName) obj;
        return ObjectUtil.typedEquals(getName(), other.getName());
    }

    // NOTE: compares according to FORMATTED name
    public int compareTo(ApplicationName o) {
        if (o == null) {
            return (-1);
        }
        
        if (this == o) {
            return 0;
        }

        return StringUtil.safeCompare(getFormatted(), o.getFormatted());
    }
    
    public ResourceKey makeKey() {
        return ResourceKey.valueOf(ResourceNames.Application, getName());
    }

    public static ApplicationName fromResourceKey(ResourceKey key) {
        key.assertIsOfType(ResourceNames.Application);
        return valueOf(key.getName());
    }
    
    /**
     * Compares {@link ApplicationName}-s using their {@link #getName()} value(s)
     */
    public static final Comparator<ApplicationName> BY_NAME_COMPARATOR=
            new Comparator<ApplicationName>() {
                public int compare(ApplicationName o1, ApplicationName o2) {
                    return StringUtil.safeCompare((o1 == null) ? null : o1.getName(),
                                                  (o2 == null) ? null : o2.getName());
                }
            };

    /**
     * Compares {@link ApplicationName}-s using their {@link #getFormatted()} value(s)
     */
    public static final Comparator<ApplicationName> BY_FORMATTED_NAME_COMPARATOR=
            new Comparator<ApplicationName>() {
                public int compare(ApplicationName o1, ApplicationName o2) {
                    return StringUtil.safeCompare((o1 == null) ? null : o1.getFormatted(),
                                                  (o2 == null) ? null : o2.getFormatted());
                }
            };
}
