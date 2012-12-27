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

import com.ebupt.webjoin.insight.intercept.operation.OperationType;


/**
 * 
 */
public final class HttpClientDefinitions {
    private HttpClientDefinitions() {
        // no instance
    }

    public static final OperationType   TYPE=OperationType.valueOf("apache-hc4");
    /**
     * Special HTTP method name used as placeholder in operations where the
     * <I>HttpRequest</I> value was <code>null</code>
     */
    public static final String  PLACEHOLDER_METHOD_NAME="<PLACEHOLDER>";
    /**
     * Special HTTP method URI value used as placeholder in operations where the
     * <I>HttpRequest</I> value was <code>null</code>
     */
    public static final String  PLACEHOLDER_URI_VALUE="http://127.0.0.1/placeholder";
}
