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
package com.ebupt.webjoin.insight.server.impl.weblogic;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes.Name;

import com.ebupt.webjoin.insight.server.impl.AbstractContainerInformationProvider;
import com.ebupt.webjoin.insight.util.FileUtil;
import com.ebupt.webjoin.insight.util.StringUtil;

 

public class WeblogicContainerInforamtionProvider extends AbstractContainerInformationProvider {
	// could have been weblogic.home, but that is correct only for the admin server
	// managed servers don't have this system property set
	public static final String WEBLOGIC_HOME = "insight.wl_home";
	public static final String WEBLOGIC_INSTANCE = "insight.instance_path_prefix";
	public static final String WEBLOGIC_TARGET_NAME = "weblogic.Name";
	public static final String WEBLOGIC      = "WebLogic";
	/**
	 * The manifest keys used to extract the WebLogic version
	 */
	public static final List<Name> VERSION_KEYS=
			Collections.unmodifiableList(Arrays.asList(Name.IMPLEMENTATION_VERSION, Name.SPECIFICATION_VERSION));
	private static final WeblogicContainerInforamtionProvider	INSTANCE=new WeblogicContainerInforamtionProvider();

	private WeblogicContainerInforamtionProvider () {
		super();
	}

	public static final WeblogicContainerInforamtionProvider getInstance () {
		return INSTANCE;
	}

	@Override
	public String getDefaultInsightBaseRoot() {
		return System.getProperty("wls.home");
	}

	@Override
	public String getDefaultInsightLogsRoot() {
		// means relative to insight.base
		return null;
	}

	@Override
	public String getInstallFolder() {
		String	folder=System.getProperty(WEBLOGIC_HOME);
		if (StringUtil.isEmpty(folder)) {
			return super.getInstallFolder();
		} else {
			return folder;
		}
	}

	@Override
	public String getInstanceFolder() {        
		String	folder=System.getProperty(WEBLOGIC_INSTANCE), target=System.getProperty(WEBLOGIC_TARGET_NAME);
		if (StringUtil.isEmpty(folder) || StringUtil.isEmpty(target)) {
			return super.getInstanceFolder();
		} else {
			return folder +  File.separator + System.getProperty(WEBLOGIC_TARGET_NAME);
		}
	}

	@Override
	public String getContainerVersion() {
		return getContainerVersion(FileUtil.buildFile(getInstallFolder(), "server", "lib", "weblogic.jar"), VERSION_KEYS);
	}

	public String getContainerName() {
		return WEBLOGIC;
	}

	@Override
	public String convertURLExternalForm(String src) {
		return FileUtil.getRelativeResourcePath(src);
	}
}
