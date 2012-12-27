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

package com.ebupt.webjoin.insight.intercept.util.version;

import com.ebupt.webjoin.insight.util.StringUtil;

 
/**
 * A set of utility methods for comparing versions.
 */
public final class InsightVersionUtil {
    private InsightVersionUtil () {
        throw new UnsupportedOperationException("No instance");
    }

    public static final int[]   EMPTY_VERSION_ARRAY={ };
    /**
     * @param version A version {@link String} - assumed to have the format
     * <code>&quot;a.b.c.d.stuff&quot;</code> where <code>a,b,c,...</code>
     * are <U>positive <B>numbers</B></U>
     * @return An <code>int[]</code> containing only the <U>numbers</U> in
     * the version (if any) discarding any &quot;stuff&quot; at the end.
     * <B>Note:</B> if <code>null/empty</code> version is provided then
     * the {@link #EMPTY_VERSION_ARRAY} is returned
     * @throws NumberFormatException if the version components are not
     * <U>positive <B>numbers</B></U>.
     */
    public static int[] toVersionArray(String version) throws NumberFormatException {
        if (StringUtil.isEmpty(version)) {
            return EMPTY_VERSION_ARRAY;
        }

        String normalizedVersion = version.replaceAll("[^0-9.]", "");
        if (StringUtil.isEmpty(normalizedVersion)) {    // can happen if no digits prefix
            return EMPTY_VERSION_ARRAY;
        }

        String [] splits = normalizedVersion.split("\\.");
        if (splits.length <= 0) {
            return EMPTY_VERSION_ARRAY;
        }

        int []versionArray = new int[splits.length];
        for (int i = 0; i < splits.length; i++) {
            String  splitValue=splits[i];
            int     verValue=Integer.parseInt(splitValue);
            if (verValue < 0) {
                throw new NumberFormatException("Negative version component (" + splitValue + ") in " + version);
            }

            versionArray[i] = verValue;
        }
        return versionArray;
    }

    /**
     * @param version 1st version string
     * @param otherVersion 2nd version string
     * @return The comparison result:</BR>
     * <UL>
     *      <LI>&gt; 0 if the first version is newer than the second</LI>
     *      <LI>&lt; 0 if the first version is older than the second</LI>
     *      <LI>0 if versions are equal</LI>
     * </UL>
     * @see #compare(int[], int[])
     */
    public static int compare(String version, String otherVersion) {
        return compare(toVersionArray(version), toVersionArray(otherVersion));
    }

    /**
     * @param current 1st version components
     * @param other 2nd version components
     * @return The comparison result:</BR>
     * <UL>
     *      <LI>&gt; 0 if the first version is newer than the second</LI>
     *      <LI>&lt; 0 if the first version is older than the second</LI>
     *      <LI>0 if versions are equal</LI>
     * </UL>
     * @see #toVersionArray(String)
     */
    public static int compare (int[] current, int[] other) {
        int curLen=(current == null) ? 0 : current.length,
            othLen=(other == null) ? 0 : other.length,
            cmnLen=Math.min(curLen, othLen);
        for (int i = 0; i < cmnLen; i++) {
            int curValue=current[i], othValue=other[i], sign=curValue - othValue;
            if (sign != 0)
                return sign;
        }

        // the shorter version comes first
        return (curLen - othLen);
    }
    /**
     * @param minVersion min. compatible version, e.g. "1.6.5-SNAPSHOT"
     * @param maxVersion max. compatible version, e.g. "1.9.0-RELEASE"
     * @param currentVersion the current version
     * @return <code>true</code> if the given version between minVersion and
     * maxVersion (inclusive). <B>Note:</B> classifiers to versions are
     * ignored; only the dot-notation is considered.
     */
    public static boolean inRange(String minVersion, String maxVersion, String currentVersion) {
        if (StringUtil.isEmpty(currentVersion)) {
            return false;
        }

        if (StringUtil.isEmpty(minVersion) && !StringUtil.isEmpty(maxVersion)) {
            return (compare(currentVersion, maxVersion) <= 0);
        }

        if (StringUtil.isEmpty(maxVersion) && !StringUtil.isEmpty(minVersion)) {
            return (compare(currentVersion, minVersion) >= 0);
        }

        return (compare(currentVersion, minVersion) >= 0) && (compare(currentVersion, maxVersion) <= 0);
    }
}
