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

package com.ebupt.webjoin.insight.intercept.operation;

/**
 * Provides a list of commonly used operation fields.
 */
public final class OperationFields {
	private OperationFields () {
		throw new UnsupportedOperationException("No instance");
	}
    /**
     * A {@link String} representation of an {@link Exception}
     */
    public static final String EXCEPTION = "exception";
    /**
     *  A {@link String}representation of a method's return value
     */
    public static final String RETURN_VALUE = "returnValue";
    /**
     * A {@link String} representation of an operation &quot;path&quot; uri
     */
    public static final String URI = "uri";

    public static final String CONTEXT_AVAILABLE = "contextAvailable";

    public static final String CONNECTION_URL = "connectionUrl";

    public static final String METHOD_NAME = "methodName";
    public static final String LINE_NUMBER = "lineNumber";
    public static final String METHOD_SIGNATURE = "methodSignature";
    public static final String CLASS_NAME = "className";
    public static final String SHORT_CLASS_NAME = "shortClassName";
    public static final String ARGUMENTS = "arguments";
    /**
     * {@link Boolean} property indicating (if exists and <code>true</code>)
     * that a frame is an endpoint analysis source
     */
    public static final String ENDPOINT_SOURCE = "epSource";
    /**
     * Mainly for UI usage. 
     * 
     * Comma separated list of operation field names containing traceId values.
     * Before frame rendering those fields will be filtered out if the their
     * trace id is not available
     */
    public static final String TRACES_FIELDS = "traces-fields";
    /**
     * Used when storing the current resource key in the operation map
     * by <U>non-parent</U> resources
     */
    public static final String OPERATION_KEY = "ResourceKey";
    /**
     * Used by parent resources only
     */
    public static final String PARENT_KEY = "ParentKey";
}
