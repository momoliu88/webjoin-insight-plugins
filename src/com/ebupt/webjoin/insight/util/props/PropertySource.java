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

package com.ebupt.webjoin.insight.util.props;

/**
 * Abstraction of {@link String} properties retrieval - inspired by Spring 3.1
 */
public interface PropertySource {
    /**
     * @param name Property name
     * @return Property value - <code>null</code> if no such property defined
     * (equivalent to {@link #getProperty(String, String)} with <code>null</code>
     * as the default value
     */
    String getProperty (String name);
    /**
     * @param name Property name
     * @param defaultValue Value to return if property not defined
     * @return The property value or the default if no such property defined
     */
    String getProperty (String name, String defaultValue);
}
