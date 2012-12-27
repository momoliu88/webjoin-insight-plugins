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

package com.ebupt.webjoin.insight.resource;

/**
 * This class contains references to commonly used ResourceKey type names
 */
public class ResourceNames {
    public static final String Server = "Server";
    public static final String EndPoint = "EndPoint";
    public static final String Application = "Application";
    public static final String ExternalResource = "External";
    
    public static final String ApplicationServer = Application + "." + Server;
    public static final String ApplicationEndPoint = Application + "." + EndPoint;
    public static final String ApplicationServerEndPoint = Application + "." + Server + "." + EndPoint;
    public static final String ApplicationServerExternalResource = Application + "." + Server + "." + EndPoint + "." + ExternalResource;
}
