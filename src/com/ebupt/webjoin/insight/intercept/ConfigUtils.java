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

package com.ebupt.webjoin.insight.intercept;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Logger;

import com.ebupt.webjoin.insight.server.ContainerInformationProvider;
import com.ebupt.webjoin.insight.server.ContainerType;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.PropertiesUtil;
import com.ebupt.webjoin.insight.server.ContainerDetector;;
 

public final class ConfigUtils implements NamedPropertySource {
    private static final Logger  log=Logger.getLogger(ConfigUtils.class.getName());
    private static final class LazyFieldHolder {
        @SuppressWarnings("synthetic-access")
		private static final ConfigUtils INSTANCE = new ConfigUtils();
    }

    public static final String INSIGHT_BASE             = "insight.base";
    public static final String CONF                     = "conf";
    public static final String INSIGHT                  = "insight";
    public static final String INSIGHT_PROPERTIES       = "insight.properties";
    public static final String INSIGHT_LOCAL_PROPERTIES = "insight.local.properties";
    
    private final String basePath, confPath, propsPath;
    private final File baseDir, propsFile, confDir, localPropsFile;
    private final Properties    props=new Properties(), globals=new Properties(), locals=new Properties();
    
    private ConfigUtils() {
        ContainerType 					containerType=ContainerDetector.getContainerType();
        ContainerInformationProvider	provider=containerType.getInformationProvider();
        String 							insightBase=resolveInsightBase(containerType, provider);
        if (!StringUtil.isEmpty(insightBase)) {
            this.baseDir   = new File(insightBase).getAbsoluteFile();
            this.basePath  = baseDir.getAbsolutePath();
            
            this.confDir   = new File(baseDir, CONF).getAbsoluteFile();
            this.confPath  = confDir.getAbsolutePath();
            
            this.propsFile = new File(confDir, INSIGHT_PROPERTIES);
            this.propsPath  = propsFile.getAbsolutePath();
        } else {
            this.baseDir = new File("");
            this.basePath = "";
            this.confDir = new File("");
            this.confPath = "";
            this.propsPath = "";
            this.propsFile = new File("");
        }
        
        this.localPropsFile = resloveLocalPropsFileLocation(containerType, provider);
    }

    /**
     * Resolves the location of the base configuration.
     * @return The best &quot;guess&quot; for the configuration base location - 
     * <code>null</code>/empty if system property not set and unknown container
     * type or provider cannot suggest a location
     * @see #resolveInsightBase(ContainerType, ContainerInformationProvider)
     */
    public String resolveInsightBase () {
        ContainerType 					containerType=ContainerDetector.getContainerType();
        ContainerInformationProvider	provider=containerType.getInformationProvider();
        return resolveInsightBase(containerType, provider);
    }

    /**
     * Resolves the location of the base configuration. It attempts to access
     * the {@link #INSIGHT_BASE} system property. If it is not set, then it
     * attempts to execute a &quot;best guess&quot;
     * @param containerType The detected {@link ContainerType}
     * @param provider The associated {@link ContainerInformationProvider}
     * @return The best &quot;guess&quot; for the configuration base location - 
     * <code>null</code>/empty if system property not set and unknown container
     * type or provider cannot suggest a location
     * @see #resolveDefaultInsightBase(ContainerType, ContainerInformationProvider)
     */
    public String resolveInsightBase (ContainerType containerType, ContainerInformationProvider provider) {
        String basePropValue = System.getProperty(INSIGHT_BASE);
        if (!StringUtil.isEmpty(basePropValue)) {
        	return basePropValue;
        }

        return resolveDefaultInsightBase(containerType, provider);
    }

    /**
     * Called in case the system property is not set
     * @param containerType The detected {@link ContainerType}
     * @param provider The associated {@link ContainerInformationProvider}
     * @return The best &quot;guess&quot; for the configuration base location - 
     * <code>null</code>/empty if unknown container type or provider cannot
     * suggest a location
     */
    public String resolveDefaultInsightBase (ContainerType containerType, ContainerInformationProvider provider) {
        if (containerType.isUnknown()) {
        	return null;
        }

    	String basePropValue = provider.getDefaultInsightBaseRoot();
        if (StringUtil.isEmpty(basePropValue)) {
        	return basePropValue;
        } else {
        	return basePropValue + File.separator + ConfigUtils.INSIGHT;
        }
    }

    private File resloveLocalPropsFileLocation(ContainerType containerType, ContainerInformationProvider provider) {
        if (containerType.isUnknown()) {
            warnLocalPropertiesLocation("unknown container");
            return new File("");
        }

        String 	instanceFolder=provider.getInstanceFolder();
        String  containerName=provider.getContainerName();
        String	containerVersion=provider.getContainerVersion();
        String	containerInfo=containerName + "-" + containerVersion;
        if (StringUtil.isEmpty(instanceFolder)) {
            warnLocalPropertiesLocation("no instance folder (container: "+containerInfo +")");
            return new File("");
        }

        File currentLocation = new File(instanceFolder);
        if (!currentLocation.exists()) {
        	warnLocalPropertiesLocation("instance folder ("+ instanceFolder +") doesn't exist (container: "+ containerInfo +")");
        	return new File("");
        }

        if (!currentLocation.isDirectory()) {
        	warnLocalPropertiesLocation("instance location ("+ instanceFolder +") not a folder (container: "+ containerInfo +")");
        	return new File("");
        }
        
        File localInsightFolder = new File(currentLocation, INSIGHT);
        File localConfFolder = new File(localInsightFolder, CONF);
        if ((!localConfFolder.exists()) && (!localConfFolder.mkdirs())) {
    		warnLocalPropertiesLocation("failed to make instance folder ("+ instanceFolder +" ,container: "+ containerInfo +")");
    		return new File("");
        }

        File file = new File(localConfFolder, INSIGHT_LOCAL_PROPERTIES);
        if ((!file.exists()) || file.isFile()) {
        	return file;
        }
        	
        warnLocalPropertiesLocation(file.getAbsolutePath() + " exists but it is not a file (container: " + containerInfo + ")");
		return new File("");
    }
    
    private static void warnLocalPropertiesLocation(String reason) {
        log.warning("Can't set local insight properties file, reason: " + reason);
    }

    public ContainerInformationProvider getContainerInformationProvider () {
        ContainerType containerType=ContainerDetector.getContainerType();
        return containerType.getInformationProvider();
    }

    public File getBaseDir () {
        return baseDir;
    }

    public String getBaseDirPath() {
        return basePath;
    }

    public File getConfDir () {
        return confDir;
    }

    public String getConfDirPath() {
        return confPath;
    }
    
    public File getPropsFile() {
        return propsFile;
    }

    public String getPropsFilePath() {
        return propsPath;
    }
    
    public File getLocalPropsFile() {
        return localPropsFile;
    }

    public String getProperty(String name) {
        return getProperty(name, null);
    }

    public String getProperty(String name, String defaultValue) {
        Properties  p=getInsightProperties();
        return p.getProperty(name, defaultValue);
    }

    public Collection<String> getPropertyNames() {
        return PropertiesUtil.propertiesNames(getInsightProperties());
    }

    /**
     * @return A <U>union</U> of the main and local properties - where
     * the local properties <U>override</U> the main ones
     * @see #getGlobalProperties()
     * @see #getLocalProperties()
     */
    public Properties getInsightProperties () {
        synchronized(props) {
            if (props.size() > 0) {
                return props;
            }
        }

        /*
         * NOTE: we run the risk of a race condition, but since we don't want
         * to synchronized on an I/O block there is no harm if 2 threads read
         * the same (!) configuration twice
         */
        Properties insightProps = getGlobalProperties();
        Properties localProps   = getLocalProperties();
        
        synchronized (props) {
            if (MapUtil.size(insightProps) > 0) {
                props.putAll(insightProps);
            }
            
            if (MapUtil.size(localProps) > 0) {
                props.putAll(localProps);
            }
            
            //avoid re-reading next time by placing some value
            if (MapUtil.size(props) <= 0) {
                props.put(getClass().getName(), "empty");
            }
        }
        
        return props;
    }

    /**
     * @return Only the properties in the main configuration file
     * @see #getPropsFile()
     */
    public Properties getGlobalProperties () {
    	return lazyInitProperties(globals, getPropsFile());
    }

    /**
     * @return The properties in the local configuration file (if exists)
     * @see #getLocalPropsFile()
     */
    public Properties getLocalProperties () {
    	return lazyInitProperties(locals, getLocalPropsFile());
    }

    /**
     * Load properties from a file if not already loaded.
     * @param targetProps The {@link Properties} to be updated with the file's
     * contents
     * @param file Properties {@link File} to load fro.m If the file does not
     * exist or is inaccessible, or an error occurs while reading it, then an
     * &quot;empty&quot; synthetic property is put to avoid re-reading it
     */
    private Properties lazyInitProperties(Properties targetProps, File file) {
    	synchronized(targetProps) {
    		if (targetProps.size() > 0) {
    			return targetProps;
    		}
    	}

        if ((file == null) || (!file.canRead())) {
            //avoid re-reading next time by placing some value
        	synchronized(targetProps) {
        		targetProps.put(file.getName(), "missing/inaccessible");
        	}
            return targetProps;
        }
        
        try {
            Properties	fileProps=PropertiesUtil.loadFromFile(file);
            synchronized(targetProps) {
            	if (MapUtil.size(fileProps) <= 0) {
            		targetProps.put(file.getName(), "empty");
            	} else {
            		targetProps.putAll(fileProps);
            	}
            }
        } catch (Exception e) {
            String  msg=e.getMessage();
            log.warning("Failed(" + e.getClass().getSimpleName() + ")"
                      + " to load properties from " + file
                      + ": " + msg);
            //avoid re-reading next time by placing some value
        	synchronized(targetProps) {
        		targetProps.put(e.getClass().getName(), e.getMessage());
        	}
        }

        return targetProps;
    }

    @SuppressWarnings("synthetic-access")
	public static final ConfigUtils getInstance() {
        return LazyFieldHolder.INSTANCE;
    }
}
