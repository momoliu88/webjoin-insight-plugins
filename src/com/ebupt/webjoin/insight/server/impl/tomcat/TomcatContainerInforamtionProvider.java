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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Attributes.Name;

public class TomcatContainerInforamtionProvider extends AbstractTomcatContainerInformationProvider {
    public static final String TOMCAT = "Tomcat";
    
    /**
     * The manifest keys used to extract the Tomcat version
     */
    public static final List<Name> VERSION_KEYS=
            Collections.unmodifiableList(Arrays.asList(Name.IMPLEMENTATION_VERSION, Name.SPECIFICATION_VERSION));

    private static final TomcatContainerInforamtionProvider INSTANCE=new TomcatContainerInforamtionProvider();

    private TomcatContainerInforamtionProvider () {
    	super();
    }

    public static final TomcatContainerInforamtionProvider getInstance () {
    	return INSTANCE;
    }

    public String getContainerName() {
        return TOMCAT;
    }
    
    @Override
    protected String getJarName() {
        return "catalina.jar";
    }
    
    @Override
    protected List<Name> getVersionKeys() {
        return VERSION_KEYS;
    }
}
