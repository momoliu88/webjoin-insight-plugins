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

package com.ebupt.webjoin.insight.server.impl.tomcat;

import java.util.Collection;
import java.util.jar.Attributes.Name;

import com.ebupt.webjoin.insight.server.impl.AbstractContainerInformationProvider;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

/**
 * 
 */
public abstract class AbstractTomcatContainerInformationProvider
			extends AbstractContainerInformationProvider {
    public static final String CATALINA_HOME = "catalina.home";
    public static final String CATALINA_BASE = "catalina.base";

	protected AbstractTomcatContainerInformationProvider() {
		super();
	}

    @Override
    public String getInstallFolder() {
        String	folder=System.getProperty(CATALINA_HOME);
        if (StringUtil.isEmpty(folder)) {
        	return super.getInstallFolder();
        } else {
        	return folder;
        }
    }
    
    @Override
	public String getInstanceFolder() {
    	String	folder=System.getProperty(CATALINA_BASE);
        if (StringUtil.isEmpty(folder)) {
        	return super.getInstanceFolder();
        } else {
        	return folder;
        }
    }

	@Override
	public String getContainerVersion() {
		return getContainerVersion(FileUtil.buildFile(getInstallFolder(), "lib", getJarName()), getVersionKeys());
	}

	/**
	 * @return Name of the JAR to be used for extracting the version
	 */
	protected abstract String getJarName();

	/**
	 * @return The manifest version keys {@link Name}-s to try and extract the
	 * version from - <B>Note:</B> <U>order</U> is important since <U>first</U>
	 * found non-empty key value is used
	 */
	protected abstract Collection<Name> getVersionKeys();
}
