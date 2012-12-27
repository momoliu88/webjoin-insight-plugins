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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class ArrayUtil {
	// some useful zero length arrays
	public static final Object[]	EMPTY_OBJECTS={ };
	public static final String[]	EMPTY_STRINGS={ };
	public static final Class<?>[]	EMPTY_CLASSES={ };

    private ArrayUtil() {
        throw new UnsupportedOperationException("Construction N/A");
    }

    public static <T> boolean addAll (Collection<? super T> coll, T ... values) {
        if (length(values) <= 0) {
            return false;
        }
        
        boolean result=false;
        for (T v : values) {
            if (coll.add(v)) {
                result = true;
            }
        }

        return result;
    }

    /**
     * Converts a range of array values to a {@link List}
     * @param startIndex Start index - inclusive
     * @param endIndex End index - exclusive
     * @param values The array values
     * @return A {@link List} of the values in the specified range.
     * @throws IllegalArgumentException if bad range specified
     */
    public static <T> List<T> indexRangeToList (int startIndex, int endIndex, T ... values) {
    	int	numValues=length(values), resultSize=endIndex - startIndex;
    	if ((startIndex < 0) || (endIndex < 0) || (startIndex > endIndex)
    	 || (startIndex > numValues) || (endIndex > numValues)) {
    		throw new IllegalArgumentException("Bad range: " + startIndex + "-" + endIndex);
    	}

    	if (resultSize == 0) {
    		return Collections.emptyList();
    	}

    	List<T>	result=new ArrayList<T>(resultSize);
    	for (int	index=startIndex; index < endIndex; index++) {
    		result.add(values[index]);
    	}

    	return result;
    }

    public static <T> T[] toArray (Collection<? extends T> coll, Class<T> arType) {
    	int	numElements=ListUtil.size(coll);
    	if (numElements <= 0) {
    		return null;
    	}

    	@SuppressWarnings("unchecked")
		T[]	result=(T[]) Array.newInstance(arType, numElements);
    	return coll.toArray(result);
    }

    public static <T> int length(T[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(int[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(long[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(short[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(double[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(float[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(byte[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(char[] array) {
        return array != null ? array.length : 0;
    }
    
    public static int length(boolean[] array) {
        return array != null ? array.length : 0;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> T[] addElements(T[] src, T... toAdd) {
        if (length(src) <= 0) {
            return toAdd;
        }
        
        if (length(toAdd) <= 0) {
            return src;
        }
        
        T[] newArr = (T[]) Array.newInstance(src.getClass().getComponentType(), src.length + toAdd.length);
        
        System.arraycopy(src, 0, newArr, 0, src.length);
        System.arraycopy(toAdd, 0, newArr, src.length, toAdd.length);
        
        return newArr;
    }
}
