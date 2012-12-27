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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class MathUtil {
    /**
     * A {@link List} of all the &quot;integral&quot; primitive wrappers
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<?>>   INTEGRAL_TYPES=
            Collections.unmodifiableList(Arrays.asList((Class<?>) Long.class, Integer.class, Short.class, Byte.class));
    /**
     * A {@link List} of all the non-&quot;integral&quot; primitive wrappers
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<?>>   PRECISION_TYPES=
            Collections.unmodifiableList(Arrays.asList((Class<?>) Double.class, Float.class));

    private MathUtil() {
        throw new UnsupportedOperationException("Instance N/A");
    }

    /**
     * @param v A <tt>double</tt> value
     * @return An <tt>int</tt> hash value for it using {@link #hashValue(long)}
     * on the {@link #getLongBits(double)} result
     */
    public static int hashValue (double v) {
        return hashValue(getLongBits(v));
    }

    /**
     * @param v A <tt>long</tt> value
     * @return An <tt>int</tt> hash value for it
     */
    public static int hashValue (long v) {
        return (int) (v ^ (v >>> 32));
    }
    /**
     * If value is nzero then returns its {@link Double#doubleToLongBits(double)},
     * zero otherwise
     */
    public static long getLongBits (double v) {
        if (v != +0.0d)
            return Double.doubleToLongBits(v);
        else
            return 0L;
    }
    /**
     * Calculate a pooled variance from a set of variances.  
     * 
     * See:  http://en.wikipedia.org/wiki/Pooled_variance
     */
    public static double getPooledVariance(double[] variances, long[] counts) {
        if (variances.length != counts.length) {
            throw new IllegalArgumentException("Array lengths differ");
        }
        
        if (variances.length == 0) {
            return Double.NaN;
        }
        
        if (variances.length == 1) {
            return 0.0;
        }
        
        double numerator = 0;
        double denom = 0;
        
        for (int i=0; i< variances.length; i++) {
            numerator += (counts[i] - 1.0) * variances[i];
            denom += counts[i] - 1.0;
        }
        return numerator / denom;
    }
    
    /**
     * Compares 2 {@link Number}s by checking if they are &quot;integral&quot;
     * ones. If so, then it compares their {@link Number#longValue()}-s,
     * otherwise it compares their {@link Number#doubleValue()}-s
     * @param n1 1st {@link Number} to compare
     * @param n2 2nd {@link Number} to compare
     * @return negative if 1st number is smaller, positive if greater and
     * zero if equal
     * @see #isIntegralNumber(Number)
     */
    public static int compareNumbers (Number n1, Number n2) {
        if (isIntegralNumber(n1) && isIntegralNumber(n2)) {
            return signOf(n1.longValue() - n2.longValue());
        } else {
            return Double.compare(n1.doubleValue(), n2.doubleValue());
        }
    }
    
    public static int signOf (int value) {
        if (value < 0)
            return (-1);
        if (value > 0)
            return 1;
        return 0;
    }

    public static int signOf (long value) {
        if (value < 0L)
            return (-1);
        if (value > 0L)
            return 1;
        return 0;
    }
    
    /**
     * @param n A {@link Number} to test
     * @return <code>true</true> if it is a <code>long/int/short/byte</code>
     * primitive wrapper
     */
    public static boolean isIntegralNumber (Number n) {
        return (n != null) && INTEGRAL_TYPES.contains(n.getClass());
    }

    /**
     * @param n A {@link Number} to test
     * @return <code>true</true> if it is a <code>double/float</code>
     * primitive wrapper
     */
    public static boolean isPrecisionNumber (Number n) {
        return (n != null) && PRECISION_TYPES.contains(n.getClass());
    }
}
