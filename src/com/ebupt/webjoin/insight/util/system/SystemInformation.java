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


package com.ebupt.webjoin.insight.util.system;

import java.util.Map;

/**
 * General API for collecting system information. 
 */
public interface SystemInformation {
    static final String CREATION_DATE_KEY = "date.creation";
    static final String JAVA_VERSION_KEY = "java.version";
    static final String PWD_KEY = "pwd";
    static final String JAVA_HEAP_KEY = "java.maxMemory";
    static final String NETWORK_IP_KEY = "system.net.ip";
    static final String NETWORK_MAC_KEY = "system.net.mac";
    static final String NETWORK_FALLBACK_IP_KEY = "system.net.fallback.ip";
    static final String NETWORK_CONNECTING_IP_KEY = "system.net.connecting.ip";
    static final String NETWORK_DEFAULT_ROUTE_IP_KEY = "system.net.default.route.ip";
    static final String NETWORK_NAME_KEY = "system.net.name";
    static final String NETWORK_LISTENERS_KEY = "system.net.listeners";
    static final String PID_KEY = "java.pid";
    static final String ARGUMENTS_KEY = "java.arguments";
    static final String CATALINA_BASE_KEY = "catalina.base";
    static final String INSIGHT_AGENT_VERSION_PROP = "insight.agent.version";
    static final String INSIGHT_AGENT_BUILDTIMESTAMP_PROP = "insight.agent.buildTimestamp";
    static final String INSIGHT_AGENT_LICENSE_PROP = "insight.agent.license";
    /**
     * Monitored application server installation path (e.g., CATALINA_HOME value)
     */
    static final String CONTAINER_HOME_PATH = "container.home.path";
    /**
     * Current running application server instance path (e.g., CATALINA_BASE value)
     */
    static final String CONTAINER_INSTANCE_PATH = "container.instance.path";
    /**
     * Current running application server version
     */
    static final String CONTAINER_VERSION = "container.version";
    /**
     * Current running application server type (e.g., Tomcat)
     */
    static final String CONTAINER_NAME = "container.name";

    /***
     * Returns a collection of properties associated with this {@link SystemInformation}
     * object.  The contents of the properties depends entirely on the implementation
     * of this class.
     * @param existingProperties
     */
    Map<String, String> getProperties(Map<String, String> existingProperties);

    Map<String, String> getProperties();
}
