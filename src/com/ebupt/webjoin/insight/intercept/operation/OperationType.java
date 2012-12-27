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

package com.ebupt.webjoin.insight.intercept.operation;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * Used to classify all implementations of {@link Operation}.  Essentially acts as 
 * an enumeration which can be updated at runtime (more elements of the enum can
 * be added).  
 * 
 * Used to classify Operations into discrete buckets.  In some cases, different
 * implementations of Operation may choose to identify with the same OperationType.
 */
public final class OperationType implements Serializable {
    private static final long serialVersionUID = 315004626799955595L;
    private static final Map<String, OperationType> types = new TreeMap<String, OperationType>(String.CASE_INSENSITIVE_ORDER);

    public static final OperationType UNKNOWN = valueOf("unknown");
    public static final OperationType SIMPLE = valueOf("simple");
    public static final OperationType HTTP = valueOf("http");
    public static final OperationType METHOD = valueOf("method");
    public static final OperationType WEB_REQUEST = valueOf("web_request");
    public static final OperationType REQUEST_DISPATCH = valueOf("request_dispatch");
    public static final OperationType ANNOTATED_METHOD = OperationType.valueOf("annotated_method");
    public static final OperationType APP_LIFECYCLE = OperationType.valueOf("lifecycle");

    private String name;
    /*
     * Constructor and Setter Methods package scope to allow for mutability in deserialization only
     */
    OperationType() {
        super();
    }

    private OperationType(String n) {
        this.name = n;
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

    /**
     * Find an operation type by name.
     */
    public static OperationType valueOf(String type) {
        String  name = type.toLowerCase();
        synchronized (types) {
            if (types.get(name) == null) {
                types.put(name, new OperationType(name));
            }
            return types.get(name);
        }
    }

    public String getName() {
        return name;
    }

    void setName(String n) {
        this.name = n;
    }

    public static final Comparator<OperationType> BY_NAME_COMPARATOR=new Comparator<OperationType>() {
			public int compare(OperationType o1, OperationType o2) {
				String	n1=(o1 == null) ? null : o1.getName(), n2=(o2 == null) ? null : o2.getName();
				return StringUtil.safeCompare(n1, n2);
			}
    	};

    @Override
    public String toString() {
        return "OperationType[" + getName() + "]";
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

        OperationType other = (OperationType) obj;
        return ObjectUtil.typedEquals(getName(), other.getName());
    }
}
