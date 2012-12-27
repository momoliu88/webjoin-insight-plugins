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

package com.ebupt.webjoin.insight.util;

import java.util.Comparator;

/**
 * 
 */
public final class ObjectUtil {
    private ObjectUtil() {
        throw new UnsupportedOperationException("No instance allowed");
    }

    // makes sure that only same typed objects are compared
    public static <T> boolean typedEquals (T o1, T o2) {
        if (o1 == o2)
            return true;
        if ((o1 == null) || (o2 == null))
            return false;
        return o1.equals(o2);
    }

    public static int hashCode (Object o) {
        return (o == null) ? 0 : o.hashCode(); 
    }

    // returns zero ONLY if same instance - CAVEAT EMPTOR...
    public static final Comparator<Object> OBJECT_INSTANCE_COMPARATOR=
            new Comparator<Object>() {
                public int compare(Object o1, Object o2) {
                    if (o1 == o2)
                        return 0;
                    if (o1 == null) // o2 is not null or o1 == o2
                        return (+1);
                    if (o2 == null)
                        return (-1);
                    
                    int nRes=o1.hashCode() - o2.hashCode();
                    if (nRes != 0)
                        return nRes;
                    if ((nRes=System.identityHashCode(o1) - System.identityHashCode(o2)) != 0)
                        return nRes;

                    Class<?>    c1=o1.getClass(), c2=o2.getClass();
                    if (c1 == c2)
                        throw new IllegalStateException("Exhausted all options for " + o1 + " vs. " + o2);
                    return StringUtil.safeCompare(c1.getName(), c2.getName());
                }
        };
}
