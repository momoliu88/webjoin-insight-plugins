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
package com.ebupt.webjoin.insight.server.impl.jetty;

import java.io.File;
import java.io.FileFilter;

import com.ebupt.webjoin.insight.server.impl.AbstractContainerInformationProvider;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.StringUtil;
 

public class JettyContainerInforamtionProvider extends AbstractContainerInformationProvider {
    public static final String JETTY_HOME = "jetty.home";
    public static final String JETTY      = "Jetty";
    /**
     * The {@link FileFilter} used to select Jetty JAR(s) for version extraction 
     */
    static final FileFilter	JETTY_JARS_FILTER=new FileFilter () {
			public boolean accept(File pathname) {
				String	name=pathname.getName();
				return pathname.isFile() && name.startsWith("jetty-") && FileUtil.isJarFileName(name);
			}
    	};
   	private static final JettyContainerInforamtionProvider	INSTANCE=new JettyContainerInforamtionProvider();

    private JettyContainerInforamtionProvider () {
    	super();
    }

    public static final JettyContainerInforamtionProvider getInstance () {
    	return INSTANCE;
    }

    @Override
    public String getDefaultInsightLogsRoot() {
        return System.getProperty("jetty.logs");
    }

    @Override
    public String getDefaultInsightBaseRoot() {
        return getInstallFolder();
    }


    @Override
	public String getInstallFolder() {
        String	folder=System.getProperty(JETTY_HOME);
        if (StringUtil.isEmpty(folder)) {
        	return super.getInstallFolder();
        } else {
        	return folder;
        }
    }
    
    @Override
    public String getInstanceFolder() {
        return getInstallFolder();
    }

	@Override
	public String getContainerVersion() {
		String	libFolder=FileUtil.buildFilePath(getInstallFolder(), "lib");
		String	result=FileUtil.findLongestFileNameCommonSuffix(libFolder, JETTY_JARS_FILTER);
		if (StringUtil.isEmpty(result)) {
			return null;
		}

		// the result will likely be "-x.y.z.jar"
		if (result.charAt(0) == '-') {
			result = result.substring(1);
		}

		if (FileUtil.isJarFileName(result)) {
			result = result.substring(0, result.length() - FileUtil.JAR_FILE_SUFFIX.length());
		}

		if (StringUtil.isEmpty(result)) {
			return null;
		} else {
			return result;
		}
	}

    public String getContainerName() {
        return JETTY;
    }
}
