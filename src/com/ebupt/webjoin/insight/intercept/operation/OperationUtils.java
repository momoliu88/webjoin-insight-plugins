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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.ebupt.webjoin.insight.resource.ResourceKey;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.MathUtil;
import com.ebupt.webjoin.insight.util.ObjectUtil;
import com.ebupt.webjoin.insight.util.StringFormatterUtils;
import com.ebupt.webjoin.insight.util.StringUtil;



/**
 * 
 */
public final class OperationUtils {
    private OperationUtils() {
        // no instance
    }

    /**
     * Converts the given value to one that can be stored inside an
     * {@link Operation}, {@link OperationList} or {@link OperationMap}:</BR>
     * <UL>
     *      <LI>
     *      For {@link String}, {@link Date} or primitive type wrappers the
     *      original object itself is used
     *      </LI>
     *      
     *      <LI>
     *      For {@link Enum}-s the {@link Enum#name()} value is used
     *      </LI>
     *      
     *      <LI>
     *      For any other object {@link StringFormatterUtils#formatObject(Object)}
     *      </LI>
     * </UL>
     * @param value Original value
     * @return The best possible representation of the object suitable
     * for storage inside an {@link Operation}, {@link OperationList} or
     * {@link OperationMap}
     * @throws IllegalArgumentException if attempting to resolve an
     * {@link Operation}, {@link OperationList} or {@link OperationMap}
     */
    public static Object resolveOperationObject (Object value) throws IllegalArgumentException {
        if ((value instanceof String)
          || StringFormatterUtils.isPrimitiveWrapper(value)
          || (value instanceof Date)) {
            return value;
        }

        if (value instanceof Enum<?>) {
            return ((Enum<?>) value).name();
        }

        // avoid recursive resolution
        if ((value instanceof Operation)
         || (value instanceof OperationList)
         || (value instanceof OperationMap)) {
            throw new IllegalArgumentException("Not allowed to map an object of type=" + value.getClass().getSimpleName());
        }

        return StringFormatterUtils.formatObject(value);
    }

    // the keys used by addNameValuePair in the created {@link OperationMap}
    public static final String  NAME_KEY="name", VALUE_KEY="value";
    /**
     * Adds a <code>&quot;name=value&quot;<code> to an {@link OperationList} by
     * creating an {@link OperationMap} and mapping the name to 'name' key and
     * the value to the 'value' key
     * @param list The {@link OperationList} to add to
     * @param name The <I>name</I> value
     * @param value The <I>value</I> value
     * @return The created {@link OperationMap}
     */
    public static OperationMap addNameValuePair (OperationList list, String name, String value) {
        return list.createMap()
                   .put(NAME_KEY, name)
                   .put(VALUE_KEY, value)
                   ;
    }

    /**
     * @param mainOp &quot;Main&quot; {@link Operation}
     * @param subOp &quot;Secondary&quot {@link Operation}
     * @return <code>true</code> if the &quot;main&quot; operation contains all
     * the <U>same</U> values in the &quot;secondary&quot. <B>Note:</B> this
     * is done <U>recursively</U> - i.e., if a value is a {@link Map} or a
     * {@link Collection} then it is also checked for containment
     */
    public static boolean containsAllProperties (Operation mainOp, Operation subOp) {
        return containsAllProperties(mainOp.asMap(), subOp.asMap());
    }
    
    public static boolean containsAllProperties (Map<String,?> mainProps, Map<String,?> subProps) {
        if (MapUtil.size(mainProps) < MapUtil.size(subProps)) {
            return false;
        }

        for (Map.Entry<String,?> subEntry : subProps.entrySet()) {
            String  key=subEntry.getKey();
            Object  subValue=subEntry.getValue(), mainValue=mainProps.get(key);
            if (!isEqualProperty(mainValue, subValue)) {
                return false;
            }
        }
        
        return true;
    }

    public static boolean containsAllProperties (Collection<?> mainProps, Collection<?> subProps) {
        if (ListUtil.size(mainProps) < ListUtil.size(subProps)) {
            return false;
        }

        Iterator<?> mainIter=mainProps.iterator(), subIter=subProps.iterator();
        while (subIter.hasNext()) {
            Object  subValue=subIter.next();
            if (!mainIter.hasNext()) {
                return false;
            }
            
            Object  mainValue=mainIter.next();
            if (!isEqualProperty(mainValue, subValue)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public static boolean isEqualProperty (Object mainValue, Object subValue) {
        if (subValue instanceof Map<?,?>) {
            if (!(mainValue instanceof Map<?,?>)) {
                return false;
            }
            if (!containsAllProperties((Map<String,?>) mainValue, (Map<String,?>) subValue)) {
                return false;
            }
        } else if (subValue instanceof Collection<?>) {
            if (!(mainValue instanceof Collection<?>)) {
                return false;
            }
            if (!containsAllProperties((Collection<?>) mainValue, (Collection<?>) subValue)) {
                return false;
            }
        } else {
            // JSON de-serialization replaces all integers with longs
            if ((mainValue instanceof Number) && (subValue instanceof Number)) {
                if (MathUtil.compareNumbers((Number) mainValue, (Number) subValue) != 0) {
                    return false;
                }
            }
            else if (!ObjectUtil.typedEquals(mainValue, subValue)) {
                return false;
            }
        }

        return true;
    }

	public static ResourceKey getResourceKey(Operation op, ResourceKey defaultResourceKey) {
        String resourceKeyString = op.get(OperationFields.OPERATION_KEY, String.class);
        if (StringUtil.isEmpty(resourceKeyString)) {
            // log warning (?)
            return defaultResourceKey;
        } else {
            return ResourceKey.valueOf(resourceKeyString);
        }
	}
}
