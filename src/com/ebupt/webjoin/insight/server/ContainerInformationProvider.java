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

package com.ebupt.webjoin.insight.server;


/**
 * Provides informations about the container of the application - e.g., 
 * Tomcat, JBoss, WebLogic, WebSphere, etc.
 */
public interface ContainerInformationProvider {
    /**
     * Default folder name used by {@link #getDefaultInsightLogsRoot()}
     */
    static final String LOGS_FOLDER = "logs";
    /**
     * @return The path to the location of the container's installation
     * - e.g., the value CATALINA_HOME for Tomcat, JBOSS_HOME for JBoss
     */
    String getInstallFolder ();
    /**
     * @return The current location of the <U>running instance</U> of the
     * container - e.g., the value CATALINA_BASE for Tomcat
     */
    String getInstanceFolder ();
    /**
     * @return The container version - <code>null</code> if cannot be determined
     */
    String getContainerVersion ();
    /**
     * @return The container name - <code>null</code> if cannot be determined - e.g., JBoss
     */
    String getContainerName ();

    /**
     * &quot;Canonize&quot; an url using the container's internal schema to
     * a <code>file</code> (where applicable)
     * @param src The original URL value
     * @return The canonical form fit for the specific provider
     */
    String convertURLExternalForm(String src);

    /**
     * Called if the base location of the configuration files has not been set
     * @return A default folder under which the <code><I>insight</I></code>
     * sub-folder is expected. If returns <code>null</code>/empty then exception
     * will be generated stating that no alternative has been provided. By default,
     * it returns {@link getInstanceFolder()}
     */
    String getDefaultInsightBaseRoot ();

    /**
     * Called if the location of the log files has not been set
     * @return An alternative for the location of the log files. If returns
     * <code>null</code>/empty then the <code>logs</code> sub-folder under
     * the base configuration is used. default={@link #getDefaultInsightBaseRoot()}
     * + {@link #LOGS_FOLDER}
     */
    String getDefaultInsightLogsRoot ();
}
