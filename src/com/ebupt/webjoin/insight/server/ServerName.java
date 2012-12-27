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

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.resource.ResourceName;
import com.ebupt.webjoin.insight.resource.ResourceNames;
import com.ebupt.webjoin.insight.util.StringUtil;


public class ServerName implements ResourceName, Serializable {
    private static final long serialVersionUID = -3165190230786161345L;
    private static final Pattern SANE_PATTERN=Pattern.compile("[\\p{Alnum}-\\.]+");
    private static final Map<String,ServerName> serversMap=
        Collections.synchronizedMap(new WeakHashMap<String,ServerName>());
    private String name;

    ServerName() {
        super();
    }

    private ServerName(String n) {
        this.name = n;
    }

    void setName(String n) {
        this.name = n;
    }

    /*
     * This ensures that we keep using the same reference to after deserialization.
     */    
    public Object readResolve() throws ObjectStreamException {
        if (StringUtil.isEmpty(name)) {
            throw new StreamCorruptedException("No current name value");
        }
        return valueOf(name);
    }

    public static ServerName valueOf(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("No server name value");
        }
        if (!serverNameIsSane(name)) {
            throw new IllegalArgumentException("ServerName=[" + name + "] is not sane.  Must be alpha-numeric");
        }

        ServerName  server=serversMap.get(name);
        if (server == null) {
            server = new ServerName(name);
            serversMap.put(name, server);
        }

        return server;
    }

    public static boolean serverNameIsSane(String id) {
        Matcher m=SANE_PATTERN.matcher(id);
        return m.matches();
    }

    public static final Comparator<ServerName> BY_NAME_COMPARATOR=
            new Comparator<ServerName>() {
                public int compare(ServerName o1, ServerName o2) {
                    String  n1=(o1 == null) ? null : o1.getName();
                    String	n2=(o2 == null) ? null : o2.getName();
                    return StringUtil.safeCompare(n1, n2);
                }
            };

    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ServerName other = (ServerName) obj;
        return name.equals(other.name);
    }

    public ResourceKey makeKey() {
        return ResourceKey.valueOf(ResourceNames.Server, name);
    }
    
    public static ServerName fromResourceKey(ResourceKey key) {
        key.assertIsOfType(ResourceNames.Server);
        return valueOf(key.getName());
    }
}
