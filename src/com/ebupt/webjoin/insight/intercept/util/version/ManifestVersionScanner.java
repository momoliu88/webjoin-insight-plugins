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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.intercept.util.KeyValPair;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
 

/**
 * Looks for versioning information in the META-INF/Manifest.MF files. Stores
 * all available versioning information.
 */
public class ManifestVersionScanner implements VersionScanner {
	// well known keys for name attributes
    static final List<Name> nameKeys=
    		Collections.unmodifiableList(Arrays.asList(
	            Name.EXTENSION_NAME,
	            new Name("Bundle-SymbolicName"),
	            Name.IMPLEMENTATION_TITLE,
	            new Name("Bundle-Name"),
	            Name.SPECIFICATION_TITLE));

	/**
	 *  Well known {@link List} of version attributes names
	 */
    public static final List<Name> VERSION_KEYS=
            Collections.unmodifiableList(Arrays.asList(
                Name.IMPLEMENTATION_VERSION,
                Name.SPECIFICATION_VERSION,
                new Name("Bundle-Version")));

    // Cache of artifacts by lowercase name => version string
    private final Map<String, String> versions=new ConcurrentHashMap<String, String>();

    public ManifestVersionScanner () {
        super();
    }

    private synchronized void rebuildVersions() {
        try {
            ClassLoader      cl=ClassUtil.getDefaultClassLoader(getClass());
            Enumeration<URL> resources=cl.getResources("META-INF/MANIFEST.MF");
            while (resources.hasMoreElements()) {
                URL             url=resources.nextElement();
                final Manifest  manifest;
                try {
                    InputStream in=url.openStream();
                    try {
                        manifest = new Manifest(in);
                    } finally {
                        in.close();
                    }
                } catch(IOException e) {
                    continue;   // ignored
                }

                KeyValPair<String, String> artifact=readManifest(manifest);
                if (artifact != null) {
                    versions.put(artifact.getKey().toLowerCase(), artifact.getValue());
                }
            }
        } catch (IOException e) {
            // swallow
        }
    }

    // package private for testing.
    static KeyValPair<String, String> getClassArtifact(Class<?> targetClass) {
        try {
            Manifest manifest=ClassUtil.loadContainerManifest(targetClass);
            return readManifest(manifest);
        } catch (IOException e) {
            Logger  logger=Logger.getLogger(ManifestVersionScanner.class.getName());
            logger.warning("getClassArtifact(" + targetClass + ")"
                        + " failed (" + e.getClass().getSimpleName() + ")"
                        + " to load container manifest: " + e.getMessage());
            return null;
        }
    }

    public Map<String, String> getVersionedArtifacts() {
        if (versions.size() <= 0) {
            rebuildVersions();
        }

        return new HashMap<String, String>(versions);
    }

    public boolean inRange(String minVersion, String maxVersion, Class<?> clazz, String title)
            throws NoVersionAvailableException {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getVersion(Class<?> clazz, String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }

        String  searchKey = key.toLowerCase();
        
        // Optimistically search the manifest which is owned by this
        // specific class.
        if (clazz != null) {
            KeyValPair<String, String> infoClass=getClassArtifact(clazz);
            if ((infoClass != null) && infoClass.getKey().contains(searchKey)) {
                return infoClass.getValue();
            }
        }

        // Fall back to searching all available manifests by the search key only
        if (versions.size() <= 0) {
            rebuildVersions();
        }

        for (String t : versions.keySet()) {
            if (t.contains(searchKey)) {
                return versions.get(t);
            }
        }

        return null;
    }

    static KeyValPair<String, String> readManifest(Manifest manifest) {
    	if (manifest == null) {
    		return null;
    	}

        Attributes 					attributes=manifest.getMainAttributes();
        KeyValPair<String, String>	artifact=readAttributes(attributes);
        if (artifact != null) {
        	return artifact;
        }

        // if main attributes have no answer, try the sections (if any)
        Map<String,?>	entries=manifest.getEntries();
        if (MapUtil.size(entries) <= 0) {
        	return null;
        }

        for (String section : entries.keySet()) {
        	if ((artifact=readAttributes(manifest.getAttributes(section))) != null) {
        		return artifact;
        	}
        }

        return null;	// no help from the specific sections
    }

    static KeyValPair<String, String> readAttributes(Attributes attributes) {
        String title = search(attributes, nameKeys);
        if (StringUtil.isEmpty(title)) {
            return null;
        }

        String version = search(attributes, VERSION_KEYS);
        if (StringUtil.isEmpty(version)) {
            return null;
        }

        return new KeyValPair<String, String>(title, version);
    }

    static String search(Attributes attributes, Collection<? extends Name> keySet) {
    	if ((MapUtil.size(attributes) <= 0) || (ListUtil.size(keySet) <= 0)) {
    		return null;
    	}

        for (Name key : keySet) {
            String value = attributes.getValue(key);
            if (!StringUtil.isEmpty(value)) {
                return value;
            }
        }

        return null;
    }
}
