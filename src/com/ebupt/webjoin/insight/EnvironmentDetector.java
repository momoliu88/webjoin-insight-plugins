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

package com.ebupt.webjoin.insight;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.ebupt.webjoin.insight.server.ContainerInformationProvider;
import com.ebupt.webjoin.insight.server.ContainerType;
import com.ebupt.webjoin.insight.server.ContainerDetector;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.MapUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
import com.ebupt.webjoin.insight.util.props.NamedPropertySource;
import com.ebupt.webjoin.insight.util.props.*;
import com.ebupt.webjoin.insight.util.system.*;

/**
 * Detects where the dashboard is running and sets some environment variable for it to run correctly
 */

public class EnvironmentDetector {
    private final NamedPropertySource   properties;
    private static final EnvironmentDetector    INSTANCE=new EnvironmentDetector();
    private static final String CATALINA_LOGS = "logs";
    private static final String INSIGHT_CONFIG = "webapps/ROOT/insight";
    private static final String CATALINA_BASE = "catalina.base";

    public static final EnvironmentDetector getInstance () {
        return INSTANCE;
    }

    private EnvironmentDetector() {
        this(new AggregateNamedPropertySource(
                RuntimeSystemInformation.getInstance(),
                SystemPropertiesSystemInformation.getInstance(),
                EnvironmentSystemInformation.getInstance()));
    }
    
    EnvironmentDetector (NamedPropertySource props) {
        properties = props;
    }

    /**
     * @return A {@link Map} of the fixed properties and their new values
     */
    public Map<String,String> fixEnv() {
    	Map<String,String>	fixes=isCloudFoundry() ? fixCloudFoundry() : fixGeneric();
    	if (MapUtil.size(fixes) <= 0) {
    		return fixes;
    	}
    	
    	for (Map.Entry<String,String> fixEntry : fixes.entrySet()) {
    		String	key=fixEntry.getKey(), value=fixEntry.getValue();
    		System.setProperty(key, value);
    	}

    	return fixes;
    }

    public boolean isJBoss () {
    	ContainerType	ct=ContainerDetector.getContainerType();
        return ContainerType.JBOSS.equals(ct);
    }

    public boolean isJetty () {
    	ContainerType	ct=ContainerDetector.getContainerType();
        return ContainerType.JETTY.equals(ct);
    }

    public boolean isWeblogic () {
    	ContainerType	ct=ContainerDetector.getContainerType();
        return ContainerType.WEBLOGIC.equals(ct);
    }

    public boolean isWebsphere () {
        return (properties.getProperty("was.install.root") != null);
    }

    public boolean isTomcat() {
    	ContainerType	ct=ContainerDetector.getContainerType();
        return ContainerType.TOMCAT.equals(ct);
    }

    public boolean isCloudFoundry() {
    	for (String name : properties.getPropertyNames()) {
    		if (name.startsWith("VCAP_")) {
    			return true;
    		}
    	}

    	return false;
    }

    protected Map<String,String> fixCloudFoundry () {
		String	catalinaBase=properties.getProperty(CATALINA_BASE);
		if (StringUtil.isEmpty(catalinaBase)) {
			throw new IllegalStateException(CATALINA_BASE + " poperty undefined");
		}

		String[]	props={
				InsightAgentPluginsHelper.INSIGHT_BASE, INSIGHT_CONFIG,
				InsightAgentPluginsHelper.INSIGHT_LOGS, CATALINA_LOGS
			};
        Map<String,String>	fixes=new TreeMap<String, String>();
		for (int	index=0; index < props.length; index += 2) {
			String	name=props[index], 
					subPath=props[index+1], 
					value=catalinaBase + File.separator + subPath;
    		fixes.put(name, value);
		}

		return fixes;
    }

	protected Map<String,String> fixGeneric () {
		//insight.base
    	String	insightBase=properties.getProperty(InsightAgentPluginsHelper.INSIGHT_BASE);
    	if (!StringUtil.isEmpty(insightBase)) {
    		return Collections.emptyMap();
    	}

    	ContainerInformationProvider	provider=ContainerDetector.getContainerInformationProvider();
    	String							installBase=(provider == null) ? null : provider.getInstallFolder();
    	File							installFolder=StringUtil.isEmpty(installBase) ? null : new File(installBase);
		if ((installFolder != null) && installFolder.isFile()) {
			installFolder = installFolder.getParentFile();
		}

    	if (installFolder != null) {
    		// if container file parent is same as install folder or has a 'target' sub-folder then use temp location
    		File	containerFile=ClassUtil.getClassContainerLocationFile(getClass());
    		if ((containerFile != null) && containerFile.isFile()) {
    			containerFile = containerFile.getParentFile();
    		}
    		
    		if (installFolder.equals(containerFile)) {
    			installFolder = null;
    		}
    	}

    	if (installFolder == null) {
    		installFolder = FileUtil.getTmpLocation(System.getProperty("user.name") + File.separator + "insight");
    	}

    	installBase = installFolder.getAbsolutePath();
    	return Collections.singletonMap(InsightAgentPluginsHelper.INSIGHT_BASE, installBase);
    }

    protected boolean anyDefinedProperty (String ... names) {
        for (String name : names) {
            if (properties.getProperty(name) != null) {
                return true;
            }
        }
        
        return false;
    }
}
