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

package com.ebupt.webjoin.insight.intercept.topology;

import com.ebupt.webjoin.insight.resource.ResourceKey;

 
/**
 * Formal definition for common subtypes of an ExternalResource
 */
public enum ExternalResourceType {
    DATABASE,                // hsql, mysql, oracle etc.
    FILESTORE,               // zfs, windows share, etc
    MAPSTORE,                // casandra, zookeeper
    KVSTORE,				 // Redis etc.
    DOCSTORE,                // MongoDB, CouchDB etc.
    QUEUE,                   // rabbitmq, activemq, etc.
    WEB_SERVICE,             // external apis
    WEB_SERVER,              // http client 3/4
    LDAP,                    // LDAP server
    SERVER,                  // generic network server         
    HUMAN,                   // person operating this software
    AUTHENTICATION_SERVICE,  // ldap server, openid, oauth, etc
    CACHE,                   // ehcache etc.
    EMAIL,                   // email
    OTHER;


    /**
     * Determine the ResourceType from a ResourceKey
     */
    public static ExternalResourceType valueOf(ResourceKey key) {
        String keyType = key.getSubType();
        for (ExternalResourceType type : ExternalResourceType.values()) {
            if (type.name().equals(keyType)) {
                return type;
            }
        }
        return OTHER;
    }
}
