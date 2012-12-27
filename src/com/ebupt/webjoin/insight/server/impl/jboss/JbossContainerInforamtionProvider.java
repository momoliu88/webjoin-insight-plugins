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
package com.ebupt.webjoin.insight.server.impl.jboss;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes.Name;

import com.ebupt.webjoin.insight.server.impl.AbstractContainerInformationProvider;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public class JbossContainerInforamtionProvider extends AbstractContainerInformationProvider {
    public static final String JBOSS_SERVER_HOME_DIR = "jboss.server.home.dir";
    public static final String JBOSS_HOME_DIR        = "jboss.home.dir";
    public static final String JBOSS                 = "Jboss";
    /**
     * A {@link Set} of the various URL(s) schemes used by the various JBoss
     * containers to hold references to resources
     */
    public static final Set<String>	JBOSS_URL_SCHEMES=
    		Collections.unmodifiableSet(
    				new HashSet<String>(
    						Arrays.asList("vfs", "vfszip", "vfsfile")));
    /**
     * The manifest keys used to extract the JBoss version
     */
    public static final List<Name> VERSION_KEYS=
            Collections.unmodifiableList(Arrays.asList(Name.SPECIFICATION_VERSION, Name.IMPLEMENTATION_VERSION));
    private static final JbossContainerInforamtionProvider	INSTANCE=new JbossContainerInforamtionProvider();

    private JbossContainerInforamtionProvider () {
    	super();
    }

    public static final JbossContainerInforamtionProvider getInstance () {
    	return INSTANCE;
    }

    @Override
    public String getInstallFolder() {
        String	folder=System.getProperty(JBOSS_HOME_DIR);
        if (StringUtil.isEmpty(folder)) {
        	return super.getInstallFolder();
        } else {
        	return folder;
        }
    }
    
    @Override
	public String getInstanceFolder() {
        String	folder=System.getProperty(JBOSS_SERVER_HOME_DIR);
        if (StringUtil.isEmpty(folder)) {
        	return super.getInstanceFolder();
        } else {
        	return folder;
        }
    }

	@Override
    public String getDefaultInsightBaseRoot() {
		return getInstallFolder();
	}

    @Override
    public String getDefaultInsightLogsRoot() {
        return System.getProperty("jboss.server.log.dir");
    }

	@Override
	public String getContainerVersion() {
		return getContainerVersion(FileUtil.buildFile(getInstallFolder(), "lib", "jboss-main.jar"), VERSION_KEYS);
	}

    @Override
	public String convertURLExternalForm(String src) {
		return convertURLExternalForm(src, JBOSS_URL_SCHEMES);
	}

	public String getContainerName() {
        return JBOSS;
    }
}
