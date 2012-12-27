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
package com.ebupt.webjoin.insight.intercept.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.intercept.endpoint.EndPointAnalyzer;
import com.ebupt.webjoin.insight.util.ArrayUtil;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public class PluginRuntimeDescriptorsRegistery {
    public static final Name MANIFEST_ATTR_NAME = new Name("Insight-Plugin-Descriptor");
    private static final PluginRuntimeDescriptorsRegistery INSTANCE = new PluginRuntimeDescriptorsRegistery();
    public static final String	UNKNOWN_VERSION="<UNKNOWN>";

    private final Map<String, String> waitingMap=new TreeMap<String, String>();
    private final Map<String, PluginRuntimeDescriptor> descriptorsMap=new TreeMap<String, PluginRuntimeDescriptor>();
    private final List<PluginDescriptor> pluginDescriptors=new ArrayList<PluginDescriptor>();
    private volatile boolean allRegistered ;
    private final Logger logger = Logger.getLogger(getClass().getName());
    
    PluginRuntimeDescriptorsRegistery() {
        super();
    }

    public static final PluginRuntimeDescriptorsRegistery getInstance () {
        return INSTANCE;
    }

    public void register(File pluginFile) {
        try {
            JarFile jar=new JarFile(pluginFile);
            try {
            	register(jar);
            } finally {
            	jar.close();
            }
        } catch (Exception e) {
            String msg = "Failed (" + e.getClass().getSimpleName() + ") to register plugin file: " + pluginFile.getAbsolutePath();

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, msg, e);
            } else {
                logger.warning(msg + ", reason: " + e.getMessage());
            }
        }
    }

    private void register (JarFile jar) throws Exception {
        Manifest 	manifest = jar.getManifest();
        Attributes  attrs = (manifest == null) ? null : manifest.getMainAttributes();
        String  	classname = (attrs == null) ? null : attrs.getValue(MANIFEST_ATTR_NAME);
        String  	version = (attrs == null) ? null : attrs.getValue(Name.IMPLEMENTATION_VERSION);
        if (StringUtil.isEmpty(classname)) {
        	return;
        }

        if (StringUtil.isEmpty(version)) {
        	version = UNKNOWN_VERSION;
        }

        if (allRegistered) {
        	register(classname, version);
        } else {
        	synchronized(waitingMap) {
        		waitingMap.put(classname, version);
        	}
        }
    }

    private synchronized void registerAllIfNeeded() {
        if (!allRegistered) {
            registerAll();
            allRegistered = true;
        }
    }

    private void registerAll() {
        synchronized(waitingMap) {
            for(Map.Entry<String, String> e : waitingMap.entrySet()) {
                register(e.getKey(), e.getValue());
            }

            waitingMap.clear();
        }
    }

    private PluginDescriptor register(String classname, String version) {
        try {
        	ClassLoader				cl=ClassUtil.getDefaultClassLoader(getClass());
            Class<?> 				clazz=ClassUtil.loadClassByName(cl, classname);
            PluginRuntimeDescriptor descriptor=(PluginRuntimeDescriptor) clazz.newInstance();
            descriptor.version = version;
            PluginDescriptor    pluginDescriptor=descriptor.toDescriptor();

            synchronized(descriptorsMap) {
                descriptorsMap.put(classname, descriptor);
            }

            synchronized(pluginDescriptors) {
                pluginDescriptors.add(pluginDescriptor);
            }

            if (logger.isLoggable(Level.FINE)) {
                logger.fine("register(" + classname + "/" + version + "): " + pluginDescriptor);
            }
            return pluginDescriptor;
        } catch (Exception e) {
        	String msg = "Failed (" + e.getClass().getSimpleName() + ") to register descriptor: " + classname;

            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, msg, e);
            } else {
                logger.warning(msg + ", reason: " + e.getMessage());
            }

            return null;
        }
    }

    public List<PluginRuntimeDescriptor> getPluginRuntimeDescriptors() {
        registerAllIfNeeded();

        synchronized(descriptorsMap) {
            return new ArrayList<PluginRuntimeDescriptor>(descriptorsMap.values());
        }
    }

    public List<EndPointAnalyzer> getEndPointAnalyzers() {
        registerAllIfNeeded();

        final Collection<? extends PluginRuntimeDescriptor>   descs;
        synchronized(descriptorsMap) {
            descs = descriptorsMap.values();
            if (descs.isEmpty()) {
                return Collections.emptyList();
            }
        }

        List<EndPointAnalyzer> toReturn = new ArrayList<EndPointAnalyzer>(descs.size());
        for(PluginRuntimeDescriptor desc : descs) {
            EndPointAnalyzer[] analyzers = desc.getEndPointAnalyzers();
            ArrayUtil.addAll(toReturn, analyzers);
        }
    
        return toReturn;
    }

    public List<PluginDescriptor> getPluginDescriptors() {
        registerAllIfNeeded();

        synchronized(pluginDescriptors) {
            return new ArrayList<PluginDescriptor>(pluginDescriptors);
        }
    }

    //for testing
    void reset() {
        synchronized(waitingMap) {
            waitingMap.clear();
        }

        synchronized(descriptorsMap) {
            descriptorsMap.clear();
        }

        allRegistered = false;
    }
}
