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
package com.ebupt.webjoin.insight.plugin.apache.http.hc4;

import org.apache.http.HttpRequest;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpRequest;

/**
 * 
 */
final class HttpPlaceholderRequest extends BasicHttpRequest {
    static final HttpPlaceholderRequest PLACEHOLDER=new HttpPlaceholderRequest();

    private HttpPlaceholderRequest () {
        super(HttpClientDefinitions.PLACEHOLDER_METHOD_NAME,
              HttpClientDefinitions.PLACEHOLDER_URI_VALUE,
              new ProtocolVersion("HTTP", 1, 1));
    }
    
    static HttpRequest resolveHttpRequest (Object ... args) {
        if ((args == null) || (args.length <= 0)) {
            return PLACEHOLDER;
        }
        
        for (Object argVal : args) {
            if (argVal instanceof HttpRequest) {
                return (HttpRequest) argVal;
            }
        }

        // no match found
        return PLACEHOLDER;
    }
}
