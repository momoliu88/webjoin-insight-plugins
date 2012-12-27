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

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Scans the current ClassLoader environment for software versions associated with
 * artifact titles or classes.
 */
public class VersionScannerRunner implements VersionScanner {
    private final Collection<VersionScanner> versionScanners = new LinkedList<VersionScanner>();

    public VersionScannerRunner() {
        versionScanners.add(new ManifestVersionScanner());
    }

    public void addScanner(VersionScanner scanner) {
        versionScanners.add(scanner);
    }


    public String getVersion(Class<?> clazz, String titleContains) {
        for (VersionScanner v : versionScanners) {
            String version = v.getVersion(clazz, titleContains);
            if (version != null) {
                return version;
            }
        }
        return null;
    }

    public Map<String, String> getVersionedArtifacts() {
        Map<String, String> versions = new HashMap<String, String>();
        for (VersionScanner v : versionScanners) {
            versions.putAll(v.getVersionedArtifacts());
        }
        return versions;
    }

    public boolean inRange(String minVersion, String maxVersion, Class<?> clazz, String searchKey) throws NoVersionAvailableException {
        String version = getVersion(clazz, searchKey);
        if (version != null) {
            return InsightVersionUtil.inRange(minVersion, maxVersion, version);
        } else {
            throw new NoVersionAvailableException("No version information available for artifact " + searchKey);
        }
    }
}
