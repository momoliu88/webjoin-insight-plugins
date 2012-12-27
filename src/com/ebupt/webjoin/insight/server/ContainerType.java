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

import com.ebupt.webjoin.insight.server.impl.*;
import com.ebupt.webjoin.insight.server.impl.jboss.*;
import com.ebupt.webjoin.insight.server.impl.tomcat.*;
import com.ebupt.webjoin.insight.server.impl.jetty.*;
import com.ebupt.webjoin.insight.server.impl.weblogic.*;

public enum ContainerType {
    
    /*
     * NOTE: The order of the containers is important!!!! 
     */
    
    /** 
     * Jboss container type
     */
    JBOSS("com.springsource.insight.bootstrap.jboss.JBossServerDelegate",
          "org.jboss.Main",
          JbossContainerInforamtionProvider.getInstance()),
    
    /**
     * TcServer container type
     */
    TC_SERVER("com.springsource.insight.bootstrap.tomcat.TomcatServerDelegate",
              "com.springsource.tcserver.serviceability.logging.TcServerLogManager",
              TcServerContainerInformationProvider.getInstance()),
          
    /** 
     * Tomcat container type
     */
    TOMCAT("com.springsource.insight.bootstrap.tomcat.TomcatServerDelegate",
           "org.apache.catalina.startup.Bootstrap",
           TomcatContainerInforamtionProvider.getInstance()),
    
     /** 
      * Jetty standalone container type      
      */
    JETTY("com.springsource.insight.bootstrap.jetty.JettyServerDelegate",
          "org.mortbay.start.Main",
          JettyContainerInforamtionProvider.getInstance()),
          
      /** 
       * Jetty embedded container type      
       */
    JETTY_EMBEDDED(JETTY.getServerDelegateClass() ,
                   "org.eclipse.jetty.start.Main",
                   JETTY.getInformationProvider()),
    
    /**
     * Weblogic container type
     */
    WEBLOGIC("com.springsource.insight.bootstrap.weblogic.WebLogicServerDelegate",
             "weblogic.Server",
             WeblogicContainerInforamtionProvider.getInstance()),
             
    UNKNOWN(null,
            null,
            UnknownContainerInforamtionProvider.getInstance());
    
    private final String serverDelegateClass;
    private final String containerDetectionClass;
    private final ContainerInformationProvider informationProvider;
    
    private ContainerType(String delegateClass, String detectionClass, ContainerInformationProvider infoProvider) {
        this.serverDelegateClass     = delegateClass;
        this.containerDetectionClass = detectionClass;
        this.informationProvider     = infoProvider;
    }
    
    public String getServerDelegateClass() {
        return serverDelegateClass;
    }

    public String getContainerDetectionClass() {
        return containerDetectionClass;
    }

    public ContainerInformationProvider getInformationProvider() {
        return informationProvider;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }
}
