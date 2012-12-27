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

package com.ebupt.webjoin.insight.server.impl;

import java.io.File;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.logging.Logger;


import com.ebupt.webjoin.insight.intercept.util.KeyValPair;
import com.ebupt.webjoin.insight.intercept.util.version.ManifestVersionScanner;
import com.ebupt.webjoin.insight.server.ContainerInformationProvider;
import com.ebupt.webjoin.insight.util.ClassUtil;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.ListUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
  

/**
 * Provides some default implementation for {@link ContainerInformationProvider}
 */
public abstract class AbstractContainerInformationProvider
                implements ContainerInformationProvider {
	public static final String	DEFAULT_TARGET_SCHEME="file";

    protected final Logger  _logger=Logger.getLogger(getClass().getName());
    protected final File    defaultContainer;

    protected AbstractContainerInformationProvider() {
        File    defaultLocation=null;
        try {
            defaultLocation = resolveContainerFolder(ClassUtil.getClassContainerLocationFile(getClass()));
        } catch(Exception e) {
            _logger.warning("Failed (" + e.getClass().getSimpleName() + ") to resolve default location: " + e.getMessage());
        }

        defaultContainer = defaultLocation;
    }

    public String getInstanceFolder() {
        return getInstallFolder();
    }

    public String getInstallFolder() {
        return FileUtil.resolveBestPath(defaultContainer);
    }

    public String getContainerVersion() {
    	return getContainerVersion(ClassUtil.getClassContainerLocationFile(getClass()), ManifestVersionScanner.VERSION_KEYS);
	}

    public String convertURLExternalForm(String src) {
		return src;
	}

    public String getDefaultInsightBaseRoot() {
		return getInstanceFolder();
	}

	public String getDefaultInsightLogsRoot() {
		return getDefaultInsightBaseRoot() + File.separator + LOGS_FOLDER;
	}

	protected String convertURLExternalForm(String src, Collection<String> sourceSchemes) {
    	return convertURLExternalForm(src, DEFAULT_TARGET_SCHEME, sourceSchemes);
    }

    protected String convertURLExternalForm(String src, String targetScheme, Collection<String> sourceSchemes) {
    	if (StringUtil.isEmpty(src) || StringUtil.isEmpty(targetScheme) || (ListUtil.size(sourceSchemes) <= 0)) {
    		return src;	// ignore if incomplete parameters
    	}

    	if ((src.length() > targetScheme.length())
         && src.startsWith(targetScheme)
         && (src.charAt(targetScheme.length()) == ':')) {
    		return src;	// if already using target scheme then do nothing
    	}

    	int	schemePos=src.indexOf(':');
    	if (schemePos <= 0) {
    		return src;	// ignore if not expected format
    	}

    	String	scheme=src.substring(0, schemePos);
    	if (!sourceSchemes.contains(scheme)) {
    		return src;	// ignore if not one of the expected source schemes
    	}

    	String	remainder=src.substring(schemePos);
    	return new StringBuilder(targetScheme.length() + remainder.length())
    					.append(targetScheme)
    					.append(remainder)
    				.toString();
    }

	protected String getContainerVersion (File containerFile, Collection<? extends Attributes.Name> names) {
    	if (containerFile == null) {
    		return null;
    	}

    	try {
    		KeyValPair<Attributes.Name,String>	result=
    				FileUtil.findFirstManifestAttributeValue(containerFile, names);
    		if (result != null) {
    			return result.getValue();
    		}
    	} catch(Exception e) {
            _logger.warning("Failed (" + e.getClass().getSimpleName() + ")"
            			  + " to resolve version of " + containerFile
            			  + " with " + names + ": " + e.getMessage());
    	}

    	return null;	// either not found or an exception
    }

	public static final File resolveContainerFolder (File file) {
        if (file == null) {
            return null;
        }

        File    folder=file.isDirectory() ? file : file.getParentFile();
        String  name=folder.getName();
        if ("bin".equals(name) || "lib".equals(name)) {
            return folder.getParentFile();
        } else {
            return folder;
        }
    }
}
