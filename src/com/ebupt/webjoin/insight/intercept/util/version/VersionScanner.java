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

import java.util.Map;

/**
 * Implementations of this interface scan the current classpath for
 * the current version of some software which is available.
 */
public interface VersionScanner {
    /**
     * For the given artifact name or title, attempt to find the currently
     * loaded version of the artifact.
     * 
     * @param searchKey  search key for title or name of the software package or jar
     */
    String getVersion(Class<?> clazz, String searchKey);

    /**
     * Return a map of all known artifacts and their versions.
     */
    Map<String, String> getVersionedArtifacts();
    
    /**
     * Is the version associated with the given title and class between min and max versions
     * @param minVersion min compatible version, e.g. "1.6.5-SNAPSHOT"
     * @param maxVersion max compatible version, e.g. "1.9.0-RELEASE"
     * @param searchKey  search key for title or name of the software package or jar
     * @throws NoVersionAvailableException if cannot determine a version
     */
    boolean inRange(String minVersion, String maxVersion,  Class<?> clazz, String searchKey) throws NoVersionAvailableException;
}

    
 
