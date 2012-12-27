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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;


/**
 * 
 */
public final class MapUtil {
    private MapUtil() {
        throw new UnsupportedOperationException("No instance allowed");
    }

    /**
     * Reverses the keys and values from one {@link Map} to another - i.e.,
     * the source map values become the keys, and its keys become the values 
     * @param src The source map
     * @param dst The destination map
     * @return Same instance as the destination map
     */
    public static <K,V,M extends Map<? super V,? super K>> M flip (Map<? extends K,? extends V> src, M dst) {
    	if (size(src) <= 0) {
    		return dst;
    	}

    	for (Map.Entry<? extends K,? extends V> se : src.entrySet()) {
    		K	key=se.getKey();
    		V	value=se.getValue();
    		dst.put(value, key);
    	}

    	return dst;
    }

    public static int size (Map<?,?> m) {
        return (m == null) ? 0 : m.size();
    }

    public static boolean compareMaps (Map<?,?> m1, Map<?,?> m2) {
        if (m1 == m2)
            return true;
        if ((m1 == null) || (m2 == null))
            return false;
        if (m1.size() != m2.size())
            return false;
        
        return containsAll(m1, m2) && containsAll(m2, m1);
    }

    public static boolean containsAll (Map<?,?> m, Map<?,?> subMap) {
        if (m == subMap)
            return true;
        if ((subMap == null) || subMap.isEmpty())
            return true;
        if ((m == null) || m.isEmpty())
            return false;
        
        for (Map.Entry<?,?> subEntry : subMap.entrySet()) {
            Object  subKey=subEntry.getKey(), subValue=subEntry.getValue();
            /*
             * If the associated value is null we need to distinguish between
             * it and the fact that the key does not exist in the main map
             */
            if (subValue == null) {
                if (!m.containsKey(subKey)) {
                    return false;
                }
                
                if (m.get(subKey) != null) {
                    return false;
                }
            } else {
                Object  value=m.get(subKey);
                if (!subValue.equals(value)) {
                    return false;
                }
            }
        }
        
        return true;
    }

    /**
     * @param map The {@link Map} to put the value into
     * @param key The mapping key
     * @param value The value
     * @return <code>true</code> if the value is non-<code>null</code> and has been
     * put in the map. <code>false</code> if the value is <code>null</code>, in which
     * case the value is <U>not</U> put in the map.
     */
    public static <K,V> boolean putIfNonNull (Map<K,V> map, K key, V value) {
        if (value == null) {
            return false;
        }
        
        map.put(key, value);
        return true;
    }

    /**
     * Removes all entries matching the specified keys from a given map
     * @param map The {@link Map} to be modified
     * @param keys The {@link Collection} of keys to remove
     * @return A {@link Collection} of all the non-<code>null</code> values
     * that were removed
     */
    public static <K,V> Collection<V> removeAll (Map<? extends K,? extends V> map, Collection<? extends K> keys) {
    	if ((size(map) <= 0) || (ListUtil.size(keys) <= 0)) {
    		return Collections.emptyList();
    	}

    	Collection<V>	values=null;
    	for (K key : keys) {
    		V	value=map.remove(key);
    		if (value == null) {
    			continue;
    		}

    		if (values == null) {
    			values = new LinkedList<V>();
    		}

    		values.add(value);
    	}

    	if (values == null) {
    		return Collections.emptyList();
    	} else {
    		return values;
    	}
    }
    /**
     * Creates a {@link Map.Entry} with the given key/value pair
     * @param key The key
     * @param value The associated value
     * @return {@link Map.Entry} whose {@link Map.Entry#getKey()} returns the
     * key and {@link Map.Entry#getValue()} returns the value. <B>Note:</B>
     * any attempt to call {@link Map.Entry#setValue(Object)} throws an
     * {@link UnsupportedOperationException}
     */
    public static <K,V> Map.Entry<K,V> createMapEntry (final K key, final V value) {
        return new Map.Entry<K,V>() {
            public K getKey() {
                return key;
            }

            public V getValue() {
                return value;
            }

            public V setValue(V v) {
                throw new UnsupportedOperationException("setValue(" + v + ") N/A");
            }

            @Override
            public int hashCode() {
                return ObjectUtil.hashCode(key) + ObjectUtil.hashCode(value);
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null)
                    return false;
                if (this == obj)
                    return true;
                if (!(obj instanceof Map.Entry<?,?>))
                    return false;
                
                Map.Entry<?,?>  other=(Map.Entry<?,?>) obj;
                return ObjectUtil.typedEquals(key, other.getKey())
                    && ObjectUtil.typedEquals(value, other.getValue())
                    ;
            }

            @Override
            public String toString() {
                return key + "=" + value; 
            }
        };
    }
}
